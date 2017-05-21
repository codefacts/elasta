package elasta.composer.message.handlers.builder;

import elasta.composer.message.handlers.JsonObjectMessageHandler;
import elasta.composer.message.handlers.MessageHandler;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Created by sohan on 5/21/2017.
 */
public interface InsertAllMessageHandlerBuilder {

    MessageHandler<List<JsonObject>> build();
}
