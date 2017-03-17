package elasta.orm.delete.impl;

import com.google.common.collect.ImmutableMap;
import elasta.commons.Utils;
import elasta.core.promise.impl.Promises;
import elasta.core.promise.intfs.Promise;
import elasta.orm.delete.ReloadTableDataFunction;
import elasta.orm.delete.TableToTableDependenciesMap;
import elasta.orm.delete.ex.ReloadDependencyValuesFunctionException;import elasta.orm.delete.loader.impl.DependencyDataLoaderBuilderImpl;
import elasta.orm.upsert.TableData;import elasta.sql.SqlDB;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by sohan on 3/13/2017.
 */
final public class ReloadDependencyValuesFunctionImpl implements ReloadTableDataFunction {
    final TableToTableDependenciesMap tableToTableDependenciesMap;
    final SqlDB sqlDB;

    public ReloadDependencyValuesFunctionImpl(TableToTableDependenciesMap tableToTableDependenciesMap, SqlDB sqlDB) {
        Objects.requireNonNull(tableToTableDependenciesMap);
        Objects.requireNonNull(sqlDB);
        this.tableToTableDependenciesMap = tableToTableDependenciesMap;
        this.sqlDB = sqlDB;
    }

    @Override
    public Promise<List<TableData>> reloadIfNecessary(List<TableData> tableDataList) {

        List<Promise<TableData>> promiseList = tableDataList.stream().map(tableData -> {
            List<String> columns = DependencyDataLoaderBuilderImpl.createDependencyColumns(Arrays.asList(tableData.getPrimaryColumns()), tableToTableDependenciesMap.get(tableData.getTable()));

            boolean reloadTableData = false;

            for (String column : columns) {
                reloadTableData = reloadTableData || tableData.getValues().getValue(column) == null;
            }

            if (Utils.not(reloadTableData)) {
                return Promises.of(tableData);
            }

            return sqlDB.query(tableData.getTable(), columns, primaryColumnValuesCriteria(tableData))
                .map(ResultSet::getRows)
                .map(jsonObjects -> {
                    if (jsonObjects.size() < 1) {
                        throw new ReloadDependencyValuesFunctionException("No data found for tableData '" + tableData.toString() + "'");
                    }
                    return jsonObjects.get(0);
                })
                .map(jsonObject -> new TableData(
                    tableData.getTable(),
                    tableData.getPrimaryColumns(),
                    jsonObject
                ))
                ;

        }).collect(Collectors.toList());

        return Promises.when(promiseList);
    }

    private JsonObject primaryColumnValuesCriteria(TableData tableData) {
        final ImmutableMap.Builder<String, Object> mapBuilder = ImmutableMap.builder();

        for (String primaryColumn : tableData.getPrimaryColumns()) {
            mapBuilder.put(primaryColumn, tableData.getValues().getValue(primaryColumn));
        }

        return new JsonObject(
            mapBuilder.build()
        );
    }

    public static void main(String[] asdf) {

    }
}