package elasta.orm.sql.sql.impl;

import elasta.commons.SimpleCounter;
import elasta.core.promise.impl.Promises;
import elasta.core.promise.intfs.Promise;
import elasta.orm.sql.sql.SqlExecutor;
import elasta.orm.sql.sql.core.SqlCriteria;
import elasta.orm.sql.sql.core.SqlFrom;
import elasta.orm.sql.sql.core.SqlJoin;
import elasta.orm.sql.sql.core.SqlSelection;
import elasta.vertxutils.VertxUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by sohan on 3/14/2017.
 */
final public class SqlExecutorImpl implements SqlExecutor {
    final JDBCClient jdbcClient;

    public SqlExecutorImpl(JDBCClient jdbcClient) {
        Objects.requireNonNull(jdbcClient);
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Promise<ResultSet> query(String sql) {
        return withConn(con -> Promises.exec(
            defer -> con.query(sql, VertxUtils.deferred(defer))));
    }

    @Override
    public Promise<ResultSet> query(String sql, JsonArray params) {
        return withConn(con -> Promises.exec(
            defer -> con.queryWithParams(sql, params, VertxUtils.deferred(defer))));
    }

    @Override
    public <T> Promise<T> queryScalar(String sql) {
        return query(sql).map(resultSet -> (T) resultSet.getResults().get(0).getValue(0));
    }

    @Override
    public <T> Promise<T> queryScalar(String sql, JsonArray params) {
        return query(sql, params).map(resultSet -> (T) resultSet.getResults().get(0).getValue(0));
    }

    @Override
    public Promise<Void> update(String sql) {
        return withConn(con -> Promises.exec(
            defer -> con.update(sql, VertxUtils.deferred(defer))).map(o -> null)
        );
    }

    @Override
    public Promise<Void> update(String sql, JsonArray params) {
        return withConn(con -> Promises.exec(
            defer -> con.updateWithParams(sql, params, VertxUtils.deferred(defer))).map(o -> null)
        );
    }

    @Override
    public Promise<Void> update(List<String> sqlList) {
        return execAndCommit(con -> Promises.exec(
            objectDefer -> con.batch(sqlList, VertxUtils.deferred(objectDefer))));
    }

    @Override
    public Promise<Void> update(List<String> sqlList, List<JsonArray> paramsList) {
        SimpleCounter counter = new SimpleCounter(0);
        return execAndCommit(
            con -> Promises.when(sqlList.stream()
                .map(sql -> Promises.exec(dfr -> con.updateWithParams(sql, paramsList.get(counter.value++), VertxUtils.deferred(dfr))))
                .collect(Collectors.toList())
            ).map(objects -> (Void) null));
    }

    private Promise<SQLConnection> conn() {
        return Promises.exec(defer -> {
            jdbcClient.getConnection(VertxUtils.deferred(defer));
        });
    }

    private <T> Promise<T> withConn(Function<SQLConnection, Promise<T>> function) {
        return conn()
            .mapP(
                con -> {
                    try {
                        return function.apply(con)
                            .cmp(signal -> con.close());
                    } catch (Exception e) {
                        con.close();
                        return Promises.error(e);
                    }
                }
            );
    }

    private Promise<Void> execAndCommit(Function<SQLConnection, Promise<Void>> function) {
        return conn()
            .thenP(con -> Promises.exec(voidDefer -> con.setAutoCommit(false, VertxUtils.deferred(voidDefer))))
            .mapP(
                con -> {
                    try {
                        return function.apply(con)
                            .thenP(aVoid -> Promises.exec(voidDefer -> con.commit(VertxUtils.deferred(voidDefer))))
                            .errP(e -> Promises.exec(voidDefer -> con.rollback(VertxUtils.deferred(voidDefer))))
                            .cmp(signal -> con.close());
                    } catch (Exception e) {
                        con.close();
                        return Promises.error(e);
                    }
                }
            );
    }
}
