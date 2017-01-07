package elasta.orm.nm.query;

import elasta.orm.nm.query.funcs.ParamsBuilderImpl;
import elasta.orm.nm.query.funcs.ops.ComparisionOps;
import elasta.orm.nm.query.funcs.ops.LogicalOps;
import elasta.orm.nm.query.funcs.ops.ValueHolderOps;
import io.vertx.core.json.JsonObject;

/**
 * Created by Jango on 2017-01-06.
 */
public interface Main {
    static void main(String[] args) {
        LogicalOps logicalOps = new LogicalOps() {
        };

        ValueHolderOps valueHolderOps = new ValueHolderOps() {
        };

        ComparisionOps comparisionOps = new ComparisionOps() {
        };

        Func and = logicalOps.and(
            comparisionOps.eq(
                valueHolderOps.valueOf("location"),
                valueHolderOps.valueOf("Dhaka")
            ),
            comparisionOps.lt(
                valueHolderOps.valueOf("height"),
                valueHolderOps.valueOf(8)
            ),
            logicalOps.and(
                comparisionOps.eq(
                    valueHolderOps.valueOf("name"),
                    valueHolderOps.valueOf("sohan")),
                comparisionOps.lt(
                    valueHolderOps.valueOf("salaray"),
                    valueHolderOps.valueOf(12000)),
                logicalOps.or(
                    logicalOps.not(
                        comparisionOps.gt(
                            valueHolderOps.valueOf("age"),
                            valueHolderOps.valueOf(12)
                        )
                    ),
                    comparisionOps.ne(
                        valueHolderOps.valueOf("size"),
                        valueHolderOps.valueOf("34")
                    )
                )
            )
        );

        System.out.println(and.get(String::valueOf));
    }
}