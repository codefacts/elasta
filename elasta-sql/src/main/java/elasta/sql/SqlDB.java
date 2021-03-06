package elasta.sql;

import elasta.core.promise.intfs.Promise;
import elasta.sql.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Created by Jango on 9/25/2016.
 */
public interface SqlDB {

    Promise<ResultSet> query(
        Collection<SqlSelection> sqlSelections,
        SqlFrom sqlFrom,
        Collection<SqlJoin> sqlJoins,
        Collection<SqlCriteria> sqlCriterias
    );

    Promise<Void> insertJo(String table, JsonObject jsonObject);

    Promise<Void> insertJo(String table, Collection<JsonObject> sqlList);

    Promise<Void> update(Collection<UpdateTpl> sqlList);

    Promise<Void> update(Stream<UpdateTpl> sqlList);

    Promise<Void> delete(String table, JsonObject where);

    Promise<ResultSet> query(String table, Collection<String> columns, JsonObject whereCriteria);

    Promise<Boolean> exists(String table, String primaryKey, Collection<SqlCondition> sqlConditions);

    Promise<ResultSet> query(SqlQuery sqlQuery);
}
