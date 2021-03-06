package tracker.server;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import elasta.commons.Utils;
import elasta.composer.MessageBus;
import elasta.module.ModuleSystem;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import tracker.Addresses;
import tracker.App;
import tracker.entity_config.Entities;
import tracker.impl.AppImpl;
import tracker.model.BaseModel;
import tracker.model.DeviceModel;
import tracker.model.UserModel;
import tracker.server.ex.ConfigLoaderException;
import tracker.server.generators.request.MessageHeaderGenerator;
import tracker.server.generators.response.*;
import tracker.server.generators.response.impl.AddAllHttpResponseGeneratorImpl;
import tracker.server.generators.response.impl.AddHttpResponseGeneratorImpl;
import tracker.server.impl.FileUploadRequestHandlerImpl;
import tracker.server.interceptors.AuthInterceptor;
import tracker.server.listeners.AddPositionListener;
import tracker.server.request.handlers.LoginRequestHandler;
import tracker.server.request.handlers.LogoutRequestHandler;
import tracker.server.request.handlers.RequestHandler;
import tracker.server.request.handlers.RequestProcessingErrorHandler;
import tracker.server.request.handlers.impl.DispatchingRequestHandlerImpl;
import tracker.server.request.handlers.impl.JaDispatchingRequestHandlerImpl;
import tracker.server.request.handlers.impl.JoDispatchingRequestHandlerImpl;
import tracker.server.request.handlers.impl.LongDispatchingRequestHandlerImpl;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static tracker.server.Uris.*;

/**
 * Created by sohan on 7/1/2017.
 */
public class TrackerServer {
    public static final String KEY_AUTH_TOKEN_EXPIRE_TIME = "auth_token_expire_time";
    public static final JsonObject config = loadConfig("config.json");
    public static final int DEFAULT_PORT = 152;
    public static final Vertx vertx = Vertx.vertx(
        new VertxOptions()
            .setEventLoopPoolSize(1)
            .setWorkerPoolSize(1)
            .setInternalBlockingPoolSize(1)
    );
    public static final MessageBus messageBus = messageBus(vertx);
    public static final ModuleSystem module = createModule(vertx, messageBus);

    public static void main(String[] asfd) {

        addEventHandlers();

        HttpServer server = createWebServer();

        server.listen(config.getInteger("port", DEFAULT_PORT));

        System.out.println("SERVER STARTED AT PORT: " + config.getInteger("port", DEFAULT_PORT));
    }

    static void addEventHandlers() {
        final EventBus eb = vertx.eventBus();

        eb.consumer(Addresses.post(Addresses.add(Entities.POSITION_ENTITY)), module.require(AddPositionListener.class));
    }

    static void addHandlers(Router router) {

        {
            final String loginApi = api(loginUri);

            router.post(loginApi)
                .handler(
                    reqHanlder(
                        module.require(LoginRequestHandler.class)
                    )
                );
        }

        {
            final String logoutApi = api(logoutUri);
            router.get(logoutApi)
                .handler(
                    reqHanlder(
                        module.require(LogoutRequestHandler.class)
                    )
                );
        }

        {
            final String uri = api(userUri);
            final String singularUri = singularUri(uri);

            router.post(uri).handler(addHandler(Entities.USER_ENTITY, ImmutableList.of(UserModel.id, UserModel.userId, UserModel.username)));

            router.patch(singularUri).handler(updateHandler(Entities.USER_ENTITY));

            router.delete(singularUri).handler(deleteHandler(Entities.USER_ENTITY));

            router.get(singularUri).handler(findOneHandler(Entities.USER_ENTITY));

            router.get(uri).handler(findAllHandler(Entities.USER_ENTITY));
        }

        {
            final String uri = api(deviceUri);
            final String singularUri = singularUri(uri);

            router.post(uri).handler(addHandler(Entities.DEVICE_ENTITY, ImmutableList.of(DeviceModel.id, DeviceModel.deviceId, DeviceModel.type)));

            router.get(singularUri).handler(findOneHandler(Entities.DEVICE_ENTITY));

            router.get(uri).handler(findAllHandler(Entities.DEVICE_ENTITY));
        }

        {
            final String uri = api(positionUri);
            final String singularUri = singularUri(uri);

            router.get(uri + groupByUserId).handler(reqHanlder(new DispatchingRequestHandlerImpl(
                ctx -> Utils.or(ctx.getBodyAsJson(), ServerUtils.emptyJsonObject()),
                module.require(MessageHeaderGenerator.class),
                module.require(FindAllHttpResponseGenerator.class),
                module.require(RequestProcessingErrorHandler.class),
                messageBus,
                Addresses.findAllPositionsGroupByUserId
            )));

            router.post(uri).handler(addHandler(Entities.POSITION_ENTITY, ImmutableList.of(BaseModel.id)));

            router.post(bulkUri(uri)).handler(addAllHandler(Entities.POSITION_ENTITY, ImmutableList.of(BaseModel.id)));

            router.get(singularUri).handler(findOneHandler(Entities.POSITION_ENTITY));

            router.get(uri).handler(findAllHandler(Entities.POSITION_ENTITY));
        }

        {
            final String uri = api(outletUri);
            final String singularUri = singularUri(uri);

            router.post(uri).handler(addHandler(Entities.OUTLET_ENTITY, ImmutableList.of(BaseModel.id)));

            router.post(bulkUri(uri)).handler(addAllHandler(Entities.OUTLET_ENTITY, ImmutableList.of(BaseModel.id)));

            router.get(singularUri).handler(findOneHandler(Entities.OUTLET_ENTITY));

            router.get(uri).handler(findAllHandler(Entities.OUTLET_ENTITY));
        }
    }

    static Handler<RoutingContext> findAllHandler(String entity) {
        return reqHanlder(
            new DispatchingRequestHandlerImpl(
                ctx -> {

                    String jsonCriteriaStr = ctx.request().getParam(ReqParams.query);

                    if (jsonCriteriaStr == null || jsonCriteriaStr.isEmpty()) {
                        return ServerUtils.emptyJsonObject();
                    }

                    return new JsonObject(jsonCriteriaStr);
                },
                module.require(MessageHeaderGenerator.class),
                module.require(FindAllHttpResponseGenerator.class),
                module.require(RequestProcessingErrorHandler.class),
                messageBus,
                Addresses.findAll(entity)
            )
        );
    }

    static Handler<RoutingContext> findOneHandler(String entity) {
        return reqHanlder(
            new DispatchingRequestHandlerImpl(
                ctx -> new JsonObject(
                    ImmutableMap.of(
                        BaseModel.id, Long.parseLong(ctx.pathParam(PathParams.id))
                    )
                ),
                module.require(MessageHeaderGenerator.class),
                module.require(FindOneHttpResponseGenerator.class),
                module.require(RequestProcessingErrorHandler.class),
                messageBus,
                Addresses.findOne(entity)
            )
        );
    }

    static Handler<RoutingContext> deleteHandler(String entity) {
        return reqHanlder(
            new LongDispatchingRequestHandlerImpl(
                module.require(MessageHeaderGenerator.class),
                module.require(DeleteHttpResponseGenerator.class),
                module.require(RequestProcessingErrorHandler.class),
                messageBus,
                Addresses.delete(entity),
                PathParams.id
            )
        );
    }

    static RequestHandler updateHandler(String entity) {
        return reqHanlder(
            new DispatchingRequestHandlerImpl(
                ctx -> {
                    long id = Long.parseLong(ctx.pathParam(PathParams.id));
                    return ctx.getBodyAsJson().put(BaseModel.id, id);
                },
                module.require(MessageHeaderGenerator.class),
                module.require(UpdateHttpResponseGenerator.class),
                module.require(RequestProcessingErrorHandler.class),
                messageBus,
                Addresses.update(entity),
                ServerUtils.APPLICATION_JSON
            )
        );
    }

    static RequestHandler addHandler(String entity) {
        return addHandler(entity, ImmutableList.of("id"));
    }

    static RequestHandler addHandler(String entity, Collection<String> fields) {
        return reqHanlder(
            new JoDispatchingRequestHandlerImpl(
                module.require(MessageHeaderGenerator.class),
                new AddHttpResponseGeneratorImpl(fields),
                module.require(RequestProcessingErrorHandler.class),
                messageBus,
                Addresses.add(entity)
            )
        );
    }

    static RequestHandler addAllHandler(String entity, List<String> fields) {
        return reqHanlder(
            new JaDispatchingRequestHandlerImpl(
                module.require(MessageHeaderGenerator.class),
                new AddAllHttpResponseGeneratorImpl(
                    fields
                ),
                module.require(RequestProcessingErrorHandler.class),
                messageBus,
                Addresses.addAll(entity)
            )
        );
    }

    static RequestHandler reqHanlder(RequestHandler requestHandler) {
        return RequestHandler.create(
            requestHandler,
            module.require(RequestProcessingErrorHandler.class)
        );
    }

    static HttpServer createWebServer() {

        HttpServer httpServer = vertx.createHttpServer();

        Router router = createRouter();

        addInterceptors(router);

        addHandlers(router);

        addMediaFileHandlers(router);

        addStaticFileHandlers(router);

        httpServer.requestHandler(router::accept);

        return httpServer;
    }

    static void addMediaFileHandlers(Router router) {

        router.post(upload("/*"))
            .handler(BodyHandler.create());

        router.post(upload("/*"))
            .handler(reqHanlder(
                new FileUploadRequestHandlerImpl(
                    vertx,
                    module.require(RequestProcessingErrorHandler.class),
                    ImmutableMap.of(
                        upload(androidTrackersPicturesUri), module.require(ServerConfig.class).getUploadDir() + "/android-trackers/pictures",
                        upload(outletsPicturesUri), module.require(ServerConfig.class).getUploadDir() + "/outlets/pictures"
                    ),
                    module.require(ServerConfig.class).getUploadDir(),
                    Uris.resourcesUri
                )
            ));
    }

    static void addStaticFileHandlers(Router router) {

        router.get(Uris.resourcesUri + "/*").handler(
            StaticHandler
                .create(
                    "/" + module.require(ServerConfig.class).getUploadDir()
                )
                .setDirectoryListing(true)
        );

        router.get(Uris.publicUri + "/*").handler(
            StaticHandler
                .create(
                    "/" + module.require(ServerConfig.class).getPublicDir()
                )
                .setDirectoryListing(true)
        );
    }

    static void addInterceptors(Router router) {

        router.post("/api/*").handler(BodyHandler.create());
        router.put("/api/*").handler(BodyHandler.create());
        router.patch("/api/*").handler(BodyHandler.create());
        router.delete("/api/*").handler(BodyHandler.create());

        router.route().handler(reqHanlder(
            ctx -> {
                ctx.response().putHeader("Access-Control-Allow-Origin", "*");
                ctx.response().putHeader("Access-Control-Allow-Methods", "GET, PUT, PATCH, POST, DELETE, HEAD");
                ctx.response().putHeader("Access-Control-Allow-Headers", "*");
                ctx.response().putHeader("Access-Control-Max-Age", "86400");
                ctx.next();
            }
        ));

        router.route(api("/*")).handler(reqHanlder(
            module.require(AuthInterceptor.class)
        ));
    }

    static Router createRouter() {

        Router router = Router.router(vertx);

        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);

        sockJSHandler.bridge(eventBusBridgeOptions());

        router.route("/eventbus/*").handler(sockJSHandler);

        router.route().handler(CookieHandler.create());

        SessionStore store = LocalSessionStore.create(vertx);

        SessionHandler sessionHandler = SessionHandler.create(store);

        router.route().handler(sessionHandler);

        return router;
    }

    private static BridgeOptions eventBusBridgeOptions() {

        BridgeOptions options = new BridgeOptions();

        options.setInboundPermitted(ImmutableList.of(
            new PermittedOptions().setAddress(BrowserEvents.userPositionTracking),
            new PermittedOptions().setAddress(BrowserEvents.replayUserPositions)
        ));

        options.setOutboundPermitted(ImmutableList.of(
            new PermittedOptions().setAddress(BrowserEvents.userPositionTracking),
            new PermittedOptions().setAddress(BrowserEvents.replayUserPositions)
        ));

        return options;
    }

    static MessageBus messageBus(Vertx vertx) {

        return new AppImpl(
            new App.Config(
                getDbConfig(),
                ImmutableMap.of(),
                vertx,
                1,
                10,
                "r",
                "kdheofdsys;fhrvtwo38rpcmbgbhdiig-b7wngy9gir993,vh9dte-46to3nf8gyd",
                authTokenExpireTime()
            )
        ).mesageBus();
    }

    static int authTokenExpireTime() {
        return config.getInteger(KEY_AUTH_TOKEN_EXPIRE_TIME);
    }

    static ModuleSystem createModule(Vertx vertx, MessageBus messageBus) {
        return TrackerServerExporter.exportTo(
            TrackerServerExporter.ExportToParams.builder()
                .builder(ModuleSystem.builder())
                .jdbcClient(JDBCClient.createShared(vertx, getDbConfig()))
                .messageBus(messageBus)
                .vertx(vertx)
                .authTokenExpireTime(authTokenExpireTime())
                .serverConfig(
                    ServerConfig.builder()
                        .uploadDir(config.getString("upload_directory", new File(baseDir(), "/uploads").getAbsolutePath()))
                        .publicDir(config.getString("public_content_directory", new File(baseDir(), "/public").getAbsolutePath()))
                        .build()
                )
                .build()
        ).build();
    }

    private static File baseDir() {
        return new File("").getAbsoluteFile();
    }

    static JsonObject getDbConfig() {
        return config.getJsonObject("db");
    }

    static JsonObject loadConfig(String filename) {

        try {

            final File configDir = new File("").getAbsoluteFile();

            System.out.println("configDir: " + configDir);

            File file = new File(configDir, filename);

            if (file.exists()) {

                final String jsonStr = Files.toString(file, StandardCharsets.UTF_8);

                return parseConfig(new JsonObject(jsonStr), filename);
            }

            return parseConfig(
                new JsonObject(
                    toString(TrackerServer.class.getResourceAsStream("/" + filename))
                ),
                filename
            );

        } catch (Exception e) {
            throw new ConfigLoaderException("Error loading configuration: " + e.toString(), e);
        }
    }

    static String toString(InputStream resourceAsStream) throws Exception {
        Objects.requireNonNull(resourceAsStream);
        try {
            return CharStreams.toString(new InputStreamReader(
                resourceAsStream
            ));
        } finally {
            resourceAsStream.close();
        }
    }

    static JsonObject parseConfig(JsonObject jsonObject, String filename) {

        final String profile = jsonObject.getString("profile");

        if (profile == null) {
            throw new ConfigLoaderException("No profile is specified in '" + filename + "'");
        }

        final JsonObject config = jsonObject.getJsonObject(profile);

        if (config == null) {
            throw new ConfigLoaderException("No config for profile '" + profile + "' is found in '" + filename + "'");
        }

        return config;
    }
}
