package elasta.orm.sql.sql.impl;

import java.util.Objects;

/**
 * Created by sohan on 3/14/2017.
 */
final public class MySqlSqlBuilderDialectImpl implements SqlBuilderDialect {

    public String table(String table, String alias) {
        Objects.requireNonNull(table);
        Objects.requireNonNull(alias);
        return "`" + table + "`" + (alias.isEmpty() ? "" : " " + alias);
    }

    public String column(String column, String alias) {
        Objects.requireNonNull(column);
        Objects.requireNonNull(alias);
        return (alias.isEmpty() ? "" : alias + ".") + "`" + column + "`";
    }
}
