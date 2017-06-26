package test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import elasta.criteria.json.mapping.GenericJsonToFuncConverterImpl;
import elasta.criteria.json.mapping.JsonToFuncConverterMap;
import elasta.criteria.json.mapping.impl.JsonToFuncConverterHelperImpl;
import elasta.criteria.json.mapping.impl.JsonToFuncConverterMapBuilderImpl;
import elasta.criteria.json.mapping.impl.ValueHolderOperationBuilderImpl;
import elasta.orm.BaseOrm;
import elasta.orm.builder.impl.BaseOrmBuilderImpl;
import elasta.orm.entity.EntitiesPreprocessor;
import elasta.orm.entity.EntitiesValidator;
import elasta.orm.entity.EntityMappingHelper;
import elasta.orm.entity.EntityUtils;
import elasta.orm.entity.core.Entity;
import elasta.orm.entity.impl.EntitiesPreprocessorImpl;
import elasta.orm.entity.impl.EntitiesValidatorImpl;
import elasta.orm.entity.impl.EntityMappingHelperImpl;
import elasta.orm.entity.impl.EntityValidatorImpl;
import elasta.sql.dbaction.DbInterceptors;
import elasta.sql.dbaction.impl.DbInterceptorsImpl;
import elasta.orm.query.iml.QueryExecutorImpl;
import elasta.sql.SqlDB;
import elasta.sql.SqlExecutor;
import elasta.sql.impl.BaseSqlDBImpl;
import elasta.sql.impl.SqlBuilderUtilsImpl;
import elasta.sql.impl.SqlDBImpl;
import elasta.sql.impl.SqlExecutorImpl;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import lombok.Builder;
import lombok.Value;

import java.util.Collection;
import java.util.List;

/**
 * Created by sohan on 3/22/2017.
 */
public interface Test {

    static EntityMappingHelper helper(Collection<Entity> entities) {
        return new EntityMappingHelperImpl(
            EntityUtils.toEntityNameToEntityMap(entities),
            EntityUtils.toTableToEntityMap(entities)
        );
    }

    static SqlDB dbSql(SqlExecutor sqlExecutor) {
        final SqlDBImpl dbSql = new SqlDBImpl(
            new BaseSqlDBImpl(
                sqlExecutor,
                new SqlBuilderUtilsImpl()
            ),
            new SqlBuilderUtilsImpl()
        );
        return dbSql;
    }

    static JDBCClient jdbcClient(String db, Vertx vertx) {
        return JDBCClient.createShared(vertx, new JsonObject(
            ImmutableMap.of(
                "user", "root",
                "password", "",
                "driver_class", "com.mysql.jdbc.Driver",
                "url", "jdbc:mysql://localhost/" + db
            )
        ));
    }

    static BaseOrm baseOrm(Params params) {

        validate(params.entities);

        params = new Params(
            process(params.entities),
            params.jdbcClient,
            params.getDbInterceptors() != null ? params.getDbInterceptors() : new DbInterceptorsImpl(
                ImmutableList.of(),
                ImmutableList.of()
            )
        );

        EntityMappingHelper helper = helper(params.entities);

        SqlExecutor sqlExecutor = sqlExecutor(params.jdbcClient);

        SqlDB sqlDB = dbSql(sqlExecutor);

        return new BaseOrmBuilderImpl(
            helper,
            sqlDB,
            new QueryExecutorImpl(
                helper,
                jsonToFuncConverterMap(),
                new GenericJsonToFuncConverterImpl(),
                sqlDB
            )
        ).build(params.entities);
    }

    static void validate(Collection<Entity> entities) {
        new EntitiesValidatorImpl(new EntityValidatorImpl()).validate(
            EntitiesValidator.Params.builder()
                .entities(entities)
                .entityNameToEntityMap(EntityUtils.toEntityNameToEntityMap(entities))
                .tableToTableDependencyMap(EntityUtils.toTableToTableDependencyMap(entities).getTableToTableDependencyMap())
                .build()
        );
    }

    static List<Entity> process(Collection<Entity> entities) {
        return new EntitiesPreprocessorImpl()
            .process(EntitiesPreprocessor.Params.builder()
                .entities(entities)
                .entityNameToEntityMap(EntityUtils.toEntityNameToEntityMap(entities))
                .tableToTableDependencyMap(EntityUtils.toTableToTableDependencyMap(entities).getTableToTableDependencyMap())
                .build());
    }

    static SqlExecutor sqlExecutor(JDBCClient jdbcClient) {
        return new SqlExecutorImpl(
            jdbcClient
        );
    }

    static JsonToFuncConverterMap jsonToFuncConverterMap() {
        return new JsonToFuncConverterMapBuilderImpl(
            new JsonToFuncConverterHelperImpl(
                new ValueHolderOperationBuilderImpl()
            )
        ).build();
    }

    @Value
    @Builder
    class Params {
        final Collection<Entity> entities;
        final JDBCClient jdbcClient;
        final DbInterceptors dbInterceptors;
    }
}