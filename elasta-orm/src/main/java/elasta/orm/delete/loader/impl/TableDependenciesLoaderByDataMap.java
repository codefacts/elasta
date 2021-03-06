package elasta.orm.delete.loader.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import elasta.commons.Utils;
import elasta.core.promise.impl.Promises;
import elasta.core.promise.intfs.Promise;
import elasta.orm.delete.TableToTableDataMap;
import elasta.orm.delete.impl.TableToTableDataMapImpl;
import elasta.orm.delete.loader.DependencyDataLoader;
import elasta.orm.upsert.TableData;
import io.vertx.core.json.JsonObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sohan on 3/12/2017.
 */
final public class TableDependenciesLoaderByDataMap {
    final Map<String, Collection<DependencyDataLoader>> map;
    final Map<TableData, TableData> dataMap;
    final TableDependenciesLoaderContext context;

    public TableDependenciesLoaderByDataMap(Map<String, Collection<DependencyDataLoader>> map, Map<TableData, TableData> dataMap, TableDependenciesLoaderContext context) {
        Objects.requireNonNull(map);
        Objects.requireNonNull(dataMap);
        Objects.requireNonNull(context);
        this.map = map;
        this.dataMap = dataMap;
        this.context = context;
    }

    public Promise<Void> load(TableData parentTableData) {

        if (Utils.not(map.containsKey(parentTableData.getTable()))) {
            return Promises.empty();
        }

        final Collection<DependencyDataLoader> dependencyDataLoaders = map.get(parentTableData.getTable());

        return loadTableDataRecursive(dependencyDataLoaders, parentTableData);
    }

    private Promise<Void> loadTableDataRecursive(
        Collection<DependencyDataLoader> dependencyDataLoaders,
        TableData parentTableData
    ) {

        if (context.contains(parentTableData)) {
            return Promises.empty();
        }

        context.add(parentTableData);

        putInTable(parentTableData);

        final List<Promise<List<TableData>>> promises = dependencyDataLoaders.stream()
            .map(dependencyDataLoader -> dependencyDataLoader.load(parentTableData))
            .map(
                promise -> promise
                    .thenP(
                        tableDatas -> {

                            final List<Promise<Void>> promiseList = tableDatas.stream()
                                .map(
                                    tableData -> loadTableDataRecursive(
                                        map.getOrDefault(tableData.getTable(), ImmutableList.of()),
                                        tableData
                                    ))
                                .collect(Collectors.toList());

                            return Promises.when(promiseList).map(voids -> (Void) null);

                        }))
            .collect(Collectors.toList());

        return Promises.when(promises).map(lists -> (Void) null);
    }

    private void putInTable(Map<TableData, TableData> dataMap, List<TableData> tableDatas) {

        tableDatas.forEach(this::putInTable);
    }

    private void putInTable(TableData tableData) {
        final String table = tableData.getTable();

        if (Utils.not(
            dataMap.containsKey(tableData)
        )) {
            dataMap.put(tableData, tableData);
            return;
        }

        TableData data = dataMap.get(tableData);
        dataMap.put(
            tableData,
            new TableData(
                table,
                tableData.getPrimaryColumns(),
                new JsonObject(
                    merge(tableData.getValues(), data.getValues())
                )
            )
        );
    }

    private Map<String, Object> merge(JsonObject values, JsonObject values1) {
        LinkedHashMap<String, Object> hashMap = new LinkedHashMap<>(values.getMap());
        hashMap.putAll(values1.getMap());
        return ImmutableMap.copyOf(hashMap);
    }

    public static TableToTableDataMap toTableToTableDependenciesDataMap(Map<TableData, TableData> dataMap) {
        final Map<String, Map<TableData, TableData>> map = new HashMap<>();

        dataMap.forEach((td, tableData) -> {
            final String table = tableData.getTable();
            Map<TableData, TableData> tableDataMap = map.get(table);
            if (tableDataMap == null) {
                map.put(table, tableDataMap = new HashMap<>());
            }
            tableDataMap.put(
                tableData,
                tableData
            );
        });

        return new TableToTableDataMapImpl(
            copyRecursive(map)
        );
    }

    private static Map<String, Map<TableData, TableData>> copyRecursive(Map<String, Map<TableData, TableData>> map) {
        ImmutableMap.Builder<String, Map<TableData, TableData>> mapBuilder = ImmutableMap.builder();
        map.forEach((table, tableDatas) -> {
            mapBuilder.put(table, ImmutableMap.copyOf(tableDatas));
        });
        return mapBuilder.build();
    }
}
