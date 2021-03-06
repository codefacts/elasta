package elasta.sql.core;

import lombok.Builder;
import lombok.Value;

import java.util.Objects;
import java.util.Optional;

/**
 * Created by sohan on 3/8/2017.
 */
@Value
@Builder
final public class SqlCriteria {
    final String column;
    final Object value;
    final String alias;

    public SqlCriteria(String column, Object value, String alias) {
        Objects.requireNonNull(column);
        Objects.requireNonNull(value);
        Objects.requireNonNull(alias);
        this.column = column;
        this.value = value;
        this.alias = alias;
    }
}
