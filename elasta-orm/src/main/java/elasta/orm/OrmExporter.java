package elasta.orm;

import com.google.common.collect.ImmutableList;
import elasta.criteria.funcs.ops.impl.*;
import elasta.criteria.json.mapping.GenericJsonToFuncConverterImpl;
import elasta.criteria.json.mapping.JsonToFuncConverter;
import elasta.criteria.json.mapping.JsonToFuncConverterHelper;
import elasta.criteria.json.mapping.JsonToFuncConverterMap;
import elasta.criteria.json.mapping.impl.JsonToFuncConverterHelperImpl;
import elasta.criteria.json.mapping.impl.JsonToFuncConverterMapBuilderImpl;
import elasta.criteria.json.mapping.impl.ValueHolderOperationBuilderImpl;
import elasta.module.ModuleExporter;
import elasta.module.ModuleSystemBuilder;
import elasta.orm.builder.impl.OperationMapBuilder;
import elasta.orm.entity.EntityMappingHelper;
import elasta.orm.entity.EntityUtils;
import elasta.orm.entity.core.Entity;
import elasta.orm.entity.impl.EntityMappingHelperImpl;
import elasta.sql.dbaction.DbInterceptors;
import elasta.sql.dbaction.impl.DbInterceptorsImpl;
import elasta.orm.impl.BaseOrmImpl;
import elasta.orm.impl.OrmImpl;
import elasta.orm.impl.QueryDataLoaderImpl;
import elasta.orm.query.QueryExecutor;
import elasta.orm.query.iml.QueryExecutorImpl;
import elasta.sql.SqlBuilderUtils;
import elasta.sql.SqlDB;
import elasta.sql.SqlExecutor;
import elasta.sql.impl.SqlBuilderUtilsImpl;
import elasta.sql.impl.SqlDBImpl;
import elasta.sql.impl.SqlExecutorImpl;
import io.vertx.ext.jdbc.JDBCClient;
import lombok.Builder;
import lombok.Value;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Jango on 9/14/2016.
 */
public interface OrmExporter extends ModuleExporter {

    static void exportTo(ExportToParams params) {
        Objects.requireNonNull(params);

        final ModuleSystemBuilder builder = params.getModuleSystemBuilder();
        final Collection<Entity> entities = params.getEntities();

        builder.export(Orm.class, module -> {
            new OrmImpl(
                module.require(EntityMappingHelper.class),
                module.require(BaseOrm.class),
                module.require(QueryDataLoader.class)
            );
        });

        builder.export(EntityMappingHelper.class, module -> {
            module.export(new EntityMappingHelperImpl(
                EntityUtils.toEntityNameToEntityMap(entities)
            ));
        });

        exportBaseOrm(params);

        builder.export(QueryDataLoader.class, module -> {
            module.export(
                new QueryDataLoaderImpl(
                    module.require(BaseOrm.class),
                    module.require(EntityMappingHelper.class)
                )
            );
        });
    }

    static void exportBaseOrm(ExportToParams params) {

        final ModuleSystemBuilder builder = params.getModuleSystemBuilder();
        final JDBCClient jdbcClient = params.getJdbcClient();
        final Collection<Entity> entities = params.getEntities();

        builder.export(BaseOrm.class, module -> {

            final Map<String, BaseOrmImpl.EntityOperation> operationMap = new OperationMapBuilder(
                entities,
                module.require(EntityMappingHelper.class),
                module.require(SqlDB.class)
            ).build();

            module.export(new BaseOrmImpl(
                operationMap,
                module.require(QueryExecutor.class)
            ));
        });

        builder.export(QueryExecutor.class, module -> {
            module.export(new QueryExecutorImpl(
                module.require(EntityMappingHelper.class),
                module.require(JsonToFuncConverterMap.class),
                module.require(JsonToFuncConverter.class),
                module.require(SqlDB.class)
            ));
        });

        builder.export(JsonToFuncConverterMap.class, module -> {

            module.export(
                new JsonToFuncConverterMapBuilderImpl(
                    new LogicalOpsImpl(),
                    new ComparisionOpsImpl(),
                    new ValueHolderOpsImpl(),
                    new ArrayOpsImpl(),
                    new StringOpsImpl(),
                    new FunctionalOpsImpl(),
                    module.require(JsonToFuncConverterHelper.class)
                ).build()
            );

        });

        builder.export(JsonToFuncConverterHelper.class, module -> {
            module.export(
                new JsonToFuncConverterHelperImpl(
                    new ValueHolderOperationBuilderImpl(
                        new ValueHolderOpsImpl()
                    )
                )
            );
        });

        builder.export(JsonToFuncConverter.class, module -> {
            module.export(new GenericJsonToFuncConverterImpl());
        });

        builder.export(SqlDB.class, module -> {
            module.export(new SqlDBImpl(
                module.require(SqlExecutor.class),
                module.require(SqlBuilderUtils.class)
            ));
        });

        builder.export(SqlExecutor.class, module -> module.export(new SqlExecutorImpl(
            jdbcClient
        )));

        builder.export(SqlBuilderUtils.class, module -> module.export(new SqlBuilderUtilsImpl()));

        builder.export(DbInterceptors.class, module -> module.export(
            new DbInterceptorsImpl(
                ImmutableList.of(),
                ImmutableList.of()
            )
        ));
    }

    @Value
    @Builder
    final class ExportToParams {
        final ModuleSystemBuilder moduleSystemBuilder;
        final JDBCClient jdbcClient;
        final Collection<Entity> entities;

        public ExportToParams(ModuleSystemBuilder moduleSystemBuilder, JDBCClient jdbcClient, Collection<Entity> entities) {
            Objects.requireNonNull(moduleSystemBuilder);
            Objects.requireNonNull(jdbcClient);
            Objects.requireNonNull(entities);
            this.moduleSystemBuilder = moduleSystemBuilder;
            this.jdbcClient = jdbcClient;
            this.entities = entities;
        }
    }
}
