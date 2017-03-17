package elasta.orm.upsert;

import com.google.common.collect.ImmutableList;
import elasta.orm.EntityUtils;import elasta.orm.entitymodel.*;import elasta.orm.entitymodel.columnmapping.DbColumnMapping;import elasta.orm.entitymodel.columnmapping.impl.DirectDbColumnMappingImpl;import elasta.orm.entitymodel.columnmapping.impl.IndirectDbColumnMappingImpl;import elasta.orm.entitymodel.columnmapping.impl.SimpleDbColumnMappingImpl;import elasta.orm.entitymodel.columnmapping.impl.VirtualDbColumnMappingImpl;import elasta.orm.entitymodel.impl.EntityMappingHelperImpl;import elasta.orm.EntityUtils;
import elasta.orm.entitymodel.ForeignColumnMapping;
import elasta.orm.entitymodel.columnmapping.DbColumnMapping;
import elasta.orm.entitymodel.columnmapping.impl.DirectDbColumnMappingImpl;
import elasta.orm.entitymodel.columnmapping.impl.IndirectDbColumnMappingImpl;
import elasta.orm.entitymodel.columnmapping.impl.SimpleDbColumnMappingImpl;
import elasta.orm.entitymodel.columnmapping.impl.VirtualDbColumnMappingImpl;
import elasta.orm.entitymodel.impl.EntityMappingHelperImpl;
import elasta.orm.upsert.builder.FunctionMapImpl;
import elasta.orm.upsert.builder.impl.UpsertFunctionBuilderImpl;
import elasta.orm.upsert.builder.FunctionMapImpl;import elasta.orm.upsert.builder.impl.UpsertFunctionBuilderImpl;import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.*;

/**
 * Created by Jango on 2017-01-21.
 */
public interface Main {
    public static void main(String[] sdlk) {

        EntityMappingHelper entityMappingHelper = new EntityMappingHelperImpl(EntityUtils.toEntityNameToEntityMap(entities()));

        FunctionMapImpl functionMap = new FunctionMapImpl();

        UpsertFunctionBuilderImpl upsertFunctionGenerator = new UpsertFunctionBuilderImpl(entityMappingHelper, functionMap);

        UpsertFunction upsertFunction = upsertFunctionGenerator.create("employee");

        functionMap.makeImmutable();

        HashMap<String, TableData> map = new LinkedHashMap<>();
        UpsertContextImpl upsertContext = new UpsertContextImpl(map);

        upsertFunction.upsert(
            new JsonObject()
                .put("id", "employee-id-2")
                .put("name", "sohan")
                .put(
                    "designation",
                    new JsonObject()
                        .put("id", "designation-id-1")
                        .put("name", "coder")
                        .put(
                            "employeeList",
                            new JsonArray()
                                .add(
                                    new JsonObject()
                                        .put("id", "employee-id-2")
                                        .put("name", "kony-2")
                                )
                                .add(
                                    new JsonObject()
                                        .put("id", "employee-id-3")
                                        .put("name", "mony-3")
                                )
                        )
                )
                .put(
                    "designation2",
                    new JsonObject()
                        .put("id", "designation-id-2")
                        .put("name", "coder2")
                        .put(
                            "employeeList",
                            new JsonArray()
                                .add(
                                    new JsonObject()
                                        .put("id", "employee-id-1")
                                        .put("name", "sohan")
                                        .put(
                                            "designation",
                                            new JsonObject()
                                                .put("id", "designation-id-1")
                                                .put("name", "coder")
                                                .put(
                                                    "employeeList",
                                                    new JsonArray()
                                                        .add(
                                                            new JsonObject()
                                                                .put("id", "employee-id-2")
                                                                .put("name", "kony-2")
                                                        )
                                                        .add(
                                                            new JsonObject()
                                                                .put("id", "employee-id-3")
                                                                .put("name", "mony-3")
                                                        )
                                                )
                                        )
                                        .put(
                                            "designation2",
                                            new JsonObject()
                                                .put("id", "designation-id-2")
                                                .put("name", "coder2")
                                        )
                                        .put(
                                            "designationList",
                                            new JsonArray()
                                                .add(
                                                    new JsonObject()
                                                        .put("id", "designation-id-3")
                                                        .put("name", "coder3")
                                                )
                                                .add(
                                                    new JsonObject()
                                                        .put("id", "designation-id-4")
                                                        .put("name", "coder4")
                                                )
                                                .add(
                                                    new JsonObject()
                                                        .put("id", "designation-id-5")
                                                        .put("name", "coder5")
                                                )
                                        )
                                        .put(
                                            "groupList",
                                            new JsonArray()
                                                .add(
                                                    new JsonObject()
                                                        .put("id", "group-id-3")
                                                        .put("name", "group3")
                                                )
                                                .add(
                                                    new JsonObject()
                                                        .put("id", "group-id-4")
                                                        .put("name", "group4")
                                                )
                                                .add(
                                                    new JsonObject()
                                                        .put("id", "group-id-5")
                                                        .put("name", "group5")
                                                )
                                        )
                                )
                        )
                )
                .put(
                    "designationList",
                    new JsonArray()
                        .add(
                            new JsonObject()
                                .put("id", "designation-id-3")
                                .put("name", "coder3")
                        )
                        .add(
                            new JsonObject()
                                .put("id", "designation-id-4")
                                .put("name", "coder4")
                        )
                        .add(
                            new JsonObject()
                                .put("id", "designation-id-5")
                                .put("name", "coder5")
                        )
                )
                .put(
                    "groupList",
                    new JsonArray()
                        .add(
                            new JsonObject()
                                .put("id", "group-id-3")
                                .put("name", "group3")
                        )
                        .add(
                            new JsonObject()
                                .put("id", "group-id-4")
                                .put("name", "group4")
                        )
                        .add(
                            new JsonObject()
                                .put("id", "group-id-5")
                                .put("name", "group5")
                        )
                ),
            upsertContext
        );

        map.values().forEach(tableData -> {
            System.out.println(tableData);
        });
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
                            new ForeignColumnMapping(new Column("DESIGNATION_ID", DbType.VARCHAR), new Column("ID", DbType.VARCHAR))
                        ),
                        "designation"
                    ),
                    new DirectDbColumnMappingImpl(
                        "designation".toUpperCase(),
                        "designation",
                        ImmutableList.of(
                            new ForeignColumnMapping(new Column("DESIGNATION2_ID", DbType.VARCHAR), new Column("ID", DbType.VARCHAR))
                        ),
                        "designation2"
                    ),
                    new IndirectDbColumnMappingImpl(
                        "designationList".toUpperCase(),
                        "designation",
                        "EMPLOY_DESIGNATION",
                        ImmutableList.of(
                            new ForeignColumnMapping(
                                new Column("ID", DbType.VARCHAR),
                                new Column("EMPLOYEE_ID", DbType.VARCHAR)
                            )
                        ),
                        ImmutableList.of(
                            new ForeignColumnMapping(
                                new Column("ID", DbType.VARCHAR),
                                new Column("DESIGNATION_ID", DbType.VARCHAR)
                            )
                        ),
                        "designationList"
                    ),
                    new VirtualDbColumnMappingImpl(
                        "GROUP",
                        "group",
                        ImmutableList.of(
                            new ForeignColumnMapping(
                                new Column("EMPLOYEE_ID", DbType.VARCHAR),
                                new Column("ID", DbType.VARCHAR)
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
                            new Column("DESIGNATION_ID", DbType.VARCHAR),
                            new Column("ID", DbType.VARCHAR)
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
                new Field("employee", JavaType.OBJECT)
            },
            new DbMapping(
                "GROUP",
                "ID",
                new DbColumnMapping[]{
                    new SimpleDbColumnMappingImpl(
                        "id", "ID", DbType.VARCHAR
                    ),
                    new SimpleDbColumnMappingImpl("name", "NAME", DbType.VARCHAR),
                    new DirectDbColumnMappingImpl(
                        "EMPLOYEE",
                        "employee",
                        ImmutableList.of(
                            new ForeignColumnMapping(
                                new Column("EMPLOYEE_ID", DbType.VARCHAR),
                                new Column("ID", DbType.VARCHAR)
                            )
                        ),
                        "employee"
                    )
                }
            )
        );

        return Arrays.asList(employee, designation, group);
    }
}