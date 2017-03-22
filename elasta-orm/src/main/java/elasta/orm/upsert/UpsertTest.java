package elasta.orm.upsert;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import elasta.core.promise.impl.Promises;
import elasta.orm.entity.*;
import elasta.orm.entity.core.columnmapping.DbColumnMapping;
import elasta.orm.entity.core.columnmapping.impl.DirectDbColumnMappingImpl;
import elasta.orm.entity.core.columnmapping.impl.IndirectDbColumnMappingImpl;
import elasta.orm.entity.core.*;
import elasta.orm.entity.core.ForeignColumnMapping;
import elasta.orm.entity.impl.EntityMappingHelperImpl;
import elasta.orm.upsert.builder.FunctionMapImpl;
import elasta.sql.SqlDB;
import elasta.sql.impl.SqlDBImpl;
import elasta.sql.impl.SqlBuilderUtilsImpl;
import elasta.orm.entity.EntityUtils;
import elasta.orm.entity.core.columnmapping.impl.SimpleDbColumnMappingImpl;
import elasta.orm.entity.core.columnmapping.impl.VirtualDbColumnMappingImpl;
import elasta.orm.upsert.builder.impl.UpsertFunctionBuilderImpl;
import elasta.sql.impl.SqlExecutorImpl;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Jango on 2017-01-21.
 */
public interface UpsertTest {
//    public static void main(String[] sdlk) {
//
//        EntityMappingHelper entityMappingHelper = new EntityMappingHelperImpl(EntityUtils.toEntityNameToEntityMap(entities()));
//
//        FunctionMapImpl functionMap = new FunctionMapImpl();
//
//        UpsertFunctionBuilderImpl upsertFunctionGenerator = new UpsertFunctionBuilderImpl(entityMappingHelper, functionMap);
//
//        UpsertFunction upsertFunction = upsertFunctionGenerator.build("employee");
//
//        functionMap.makeImmutable();
//
//        HashMap<String, TableData> map = new LinkedHashMap<>();
//        UpsertContextImpl upsertContext = new UpsertContextImpl(map);
//
//        upsertFunction.upsert(
//            new JsonObject()
//                .put("id", "employee-id-2")
//                .put("name", "sohan")
//                .put(
//                    "designation",
//                    new JsonObject()
//                        .put("id", "designation-id-1")
//                        .put("name", "coder")
//                        .put(
//                            "employeeList",
//                            new JsonArray()
//                                .add(
//                                    new JsonObject()
//                                        .put("id", "employee-id-2")
//                                        .put("name", "kony-2")
//                                )
//                                .add(
//                                    new JsonObject()
//                                        .put("id", "employee-id-3")
//                                        .put("name", "mony-3")
//                                )
//                        )
//                )
//                .put(
//                    "designation2",
//                    new JsonObject()
//                        .put("id", "designation-id-2")
//                        .put("name", "coder2")
//                        .put(
//                            "employeeList",
//                            new JsonArray()
//                                .add(
//                                    new JsonObject()
//                                        .put("id", "employee-id-1")
//                                        .put("name", "sohan")
//                                        .put(
//                                            "designation",
//                                            new JsonObject()
//                                                .put("id", "designation-id-1")
//                                                .put("name", "coder")
//                                                .put(
//                                                    "employeeList",
//                                                    new JsonArray()
//                                                        .add(
//                                                            new JsonObject()
//                                                                .put("id", "employee-id-2")
//                                                                .put("name", "kony-2")
//                                                        )
//                                                        .add(
//                                                            new JsonObject()
//                                                                .put("id", "employee-id-3")
//                                                                .put("name", "mony-3")
//                                                        )
//                                                )
//                                        )
//                                        .put(
//                                            "designation2",
//                                            new JsonObject()
//                                                .put("id", "designation-id-2")
//                                                .put("name", "coder2")
//                                        )
//                                        .put(
//                                            "designationList",
//                                            new JsonArray()
//                                                .add(
//                                                    new JsonObject()
//                                                        .put("id", "designation-id-3")
//                                                        .put("name", "coder3")
//                                                )
//                                                .add(
//                                                    new JsonObject()
//                                                        .put("id", "designation-id-4")
//                                                        .put("name", "coder4")
//                                                )
//                                                .add(
//                                                    new JsonObject()
//                                                        .put("id", "designation-id-5")
//                                                        .put("name", "coder5")
//                                                )
//                                        )
//                                        .put(
//                                            "groupList",
//                                            new JsonArray()
//                                                .add(
//                                                    new JsonObject()
//                                                        .put("id", "group-id-3")
//                                                        .put("name", "group3")
//                                                )
//                                                .add(
//                                                    new JsonObject()
//                                                        .put("id", "group-id-4")
//                                                        .put("name", "group4")
//                                                )
//                                                .add(
//                                                    new JsonObject()
//                                                        .put("id", "group-id-5")
//                                                        .put("name", "group5")
//                                                )
//                                        )
//                                )
//                        )
//                )
//                .put(
//                    "designationList",
//                    new JsonArray()
//                        .add(
//                            new JsonObject()
//                                .put("id", "designation-id-3")
//                                .put("name", "coder3")
//                        )
//                        .add(
//                            new JsonObject()
//                                .put("id", "designation-id-4")
//                                .put("name", "coder4")
//                        )
//                        .add(
//                            new JsonObject()
//                                .put("id", "designation-id-5")
//                                .put("name", "coder5")
//                        )
//                )
//                .put(
//                    "groupList",
//                    new JsonArray()
//                        .add(
//                            new JsonObject()
//                                .put("id", "group-id-3")
//                                .put("name", "group3")
//                        )
//                        .add(
//                            new JsonObject()
//                                .put("id", "group-id-4")
//                                .put("name", "group4")
//                        )
//                        .add(
//                            new JsonObject()
//                                .put("id", "group-id-5")
//                                .put("name", "group5")
//                        )
//                ),
//            upsertContext
//        );
//
//        map.values().forEach(System.out::println);
//
//        final Vertx vertx = Vertx.vertx();
//
//        final SqlDB sqlDB = dbSql("orm", vertx);
//
//        vertx.setTimer(1, event -> {
//            Promises.when(map.values().stream().map(tableData -> {
//                return sqlDB.insertJo(tableData.getTable(), tableData.getValues()).then(aVoid -> System.out.println("complete: " + tableData));
//            }).collect(Collectors.toList())).then(voids -> {
//                System.out.println("Insert complete");
//            }).err(throwable -> throwable.printStackTrace())
//            ;
//        });
//    }

    static SqlDB dbSql(String db, Vertx vertx) {
        final SqlDBImpl dbSql = new SqlDBImpl(
            new SqlExecutorImpl(
                JDBCClient.createShared(vertx, new JsonObject(
                    ImmutableMap.of(
                        "user", "root",
                        "password", "",
                        "driver_class", "com.mysql.jdbc.Driver",
                        "url", "jdbc:mysql://localhost/" + db
                    )
                ))
            ),
            new SqlBuilderUtilsImpl()
        );
        return dbSql;
    }

    static SqlDB dbSql(String db) {
        return dbSql(db, Vertx.vertx());
    }

    static Collection<Entity> entities() {

        Entity employee = new Entity(
            "employee",
            "id",
            new Field[]{
                new Field("id", JavaType.STRING, Optional.empty()),
                new Field("name", JavaType.STRING, Optional.empty()),
                new Field("designation", JavaType.OBJECT, Optional.of(
                    new Relationship(Relationship.Type.MANY_TO_ONE, Relationship.Name.HAS_ONE, "designation")
                )),
                new Field("designation2", JavaType.OBJECT, Optional.of(
                    new Relationship(Relationship.Type.ONE_TO_ONE, Relationship.Name.HAS_ONE, "designation")
                )),
                new Field("designationList", JavaType.ARRAY, Optional.of(
                    new Relationship(Relationship.Type.ONE_TO_MANY, Relationship.Name.HAS_MANY, "designation")
                )),
                new Field("groupList", JavaType.ARRAY, Optional.of(
                    new Relationship(Relationship.Type.ONE_TO_MANY, Relationship.Name.HAS_MANY, "group")
                ))
            },
            new DbMapping(
                "EMPLOYEE",
                "ID",
                new DbColumnMapping[]{
                    new SimpleDbColumnMappingImpl("id", "ID", DbType.VARCHAR),
                    new SimpleDbColumnMappingImpl("name", "NAME", DbType.VARCHAR),
                    new DirectDbColumnMappingImpl(
                        "designation".toUpperCase(),
                        "designation",
                        ImmutableList.of(
                            new ForeignColumnMapping("DESIGNATION_ID", "ID")
                        ),
                        "designation"
                    ),
                    new DirectDbColumnMappingImpl(
                        "designation".toUpperCase(),
                        "designation",
                        ImmutableList.of(
                            new ForeignColumnMapping("DESIGNATION2_NAME", "NAME")
                        ),
                        "designation2"
                    ),
                    new IndirectDbColumnMappingImpl(
                        "designation".toUpperCase(),
                        "designation",
                        "EMPLOY_DESIGNATION",
                        ImmutableList.of(
                            new ForeignColumnMapping(
                                "ID",
                                "EMPLOYEE_ID"
                            )
                        ),
                        ImmutableList.of(
                            new ForeignColumnMapping(
                                "ID",
                                "DESIGNATION_ID"
                            )
                        ),
                        "designationList"
                    ),
                    new VirtualDbColumnMappingImpl(
                        "GROUP",
                        "group",
                        ImmutableList.of(
                            new ForeignColumnMapping(
                                "EMPLOYEE_NAME",
                                "NAME"
                            )
                        ),
                        "groupList"
                    )
                }
            )
        );

        Entity designation = new Entity(
            "designation",
            "id",
            new Field[]{
                new Field("id", JavaType.STRING),
                new Field("name", JavaType.STRING),
                new Field("employeeList", JavaType.ARRAY, Optional.of(
                    new Relationship(Relationship.Type.ONE_TO_MANY, Relationship.Name.HAS_MANY, "employee")
                ))
            },
            new DbMapping(
                "designation".toUpperCase(),
                "ID",
                new DbColumnMapping[]{
                    new SimpleDbColumnMappingImpl("id", "ID", DbType.VARCHAR),
                    new SimpleDbColumnMappingImpl("name", "NAME", DbType.VARCHAR),
                    new VirtualDbColumnMappingImpl("EMPLOYEE", "employee", ImmutableList.of(
                        new ForeignColumnMapping(
                            "DESIGNATION_ID",
                            "ID"
                        )
                    ), "employeeList")
                }
            )
        );

        Entity group = new Entity(
            "group",
            "id",
            new Field[]{
                new Field("id", JavaType.STRING),
                new Field("name", JavaType.STRING),
                new Field("employee", JavaType.OBJECT, Optional.of(
                    new Relationship(
                        Relationship.Type.MANY_TO_ONE,
                        Relationship.Name.HAS_ONE,
                        "employee"
                    )
                ))
            },
            new DbMapping(
                "GROUP",
                "ID",
                new DbColumnMapping[]{
                    new SimpleDbColumnMappingImpl("id", "ID", DbType.VARCHAR),
                    new SimpleDbColumnMappingImpl("name", "NAME", DbType.VARCHAR),
                    new DirectDbColumnMappingImpl(
                        "EMPLOYEE",
                        "employee",
                        ImmutableList.of(
                            new ForeignColumnMapping(
                                "EMPLOYEE_NAME",
                                "NAME"
                            )
                        ),
                        "employee"
                    )
                }
            )
        );

        return Arrays.asList(employee, designation, group);
    }

    static EntityMappingHelper helper() {
        return new EntityMappingHelperImpl(
            EntityUtils.toEntityNameToEntityMap(entities())
        );
    }
}
