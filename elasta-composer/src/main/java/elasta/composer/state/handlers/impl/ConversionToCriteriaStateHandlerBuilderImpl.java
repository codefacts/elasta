package elasta.composer.state.handlers.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import elasta.composer.Events;
import elasta.composer.MsgEnterEventHandlerP;
import elasta.composer.state.handlers.ConversionToCriteriaStateHandlerBuilder;
import elasta.core.flow.EnterEventHandlerP;
import elasta.core.flow.Flow;
import elasta.core.promise.impl.Promises;
import elasta.criteria.Func;
import elasta.criteria.funcs.ops.impl.LogicalOpsImpl;
import elasta.orm.impl.OperatorUtils;
import elasta.orm.query.expression.FieldExpression;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

/**
 * Created by sohan on 5/12/2017.
 */
final public class ConversionToCriteriaStateHandlerBuilderImpl implements ConversionToCriteriaStateHandlerBuilder {
    final String alias;

    public ConversionToCriteriaStateHandlerBuilderImpl(String alias) {
        Objects.requireNonNull(alias);
        this.alias = alias;
    }

    @Override
    public MsgEnterEventHandlerP<JsonObject, JsonObject> build() {
        return msg -> {

            final JsonObject criteria = toCriteria(msg.body());

            return Promises.of(
                Flow.trigger(Events.next, msg.withBody(
                    criteria
                ))
            );
        };
    }

    private JsonObject toCriteria(JsonObject criteria) {

        ImmutableList.Builder<JsonObject> criteriaListBuilder = ImmutableList.builder();

        criteria.getMap().forEach((fieldName, value) -> {
            criteriaListBuilder.add(
                OperatorUtils.eq(alias + "." + fieldName, value)
            );
        });

        return OperatorUtils.and(criteriaListBuilder.build());
    }
}
