package elasta.orm.nm.query.builder;

import com.google.common.collect.ImmutableMap;
import elasta.orm.nm.criteria.ParamsBuilder;
import elasta.orm.nm.query.FieldExpression;
import elasta.orm.nm.query.builder.ex.FieldExpressionResolverException;
import elasta.orm.nm.query.builder.ex.NoOpsFieldExpressionHolderFuncException;

import java.util.Map;
import java.util.Objects;

/**
 * Created by Jango on 17/02/10.
 */
final public class FieldExpressionResolverImpl implements FieldExpressionResolver {
    Map<FieldExpression, FieldExpressionHolderFunc> funcMap;

    public FieldExpressionResolverImpl(Map<FieldExpression, FieldExpressionHolderFunc> funcMap) {
        Objects.requireNonNull(funcMap);
        this.funcMap = funcMap;
    }

    @Override
    public String resolve(FieldExpression fieldExpression, ParamsBuilder paramsBuilder) {

        FieldExpressionHolderFunc func = funcMap.get(fieldExpression);

        if (func == null) {
            throw new FieldExpressionResolverException("No FieldExpressionHolderFunc found for " + fieldExpression);
        }

        return func.get(paramsBuilder);
    }

    public FieldExpressionResolverImpl setFuncMap(Map<FieldExpression, FieldExpressionHolderFunc> funcMap) {
        Objects.requireNonNull(funcMap);
        this.funcMap = funcMap;
        return this;
    }

    public Map<FieldExpression, FieldExpressionHolderFunc> getFuncMap() {
        return funcMap;
    }

    public FieldExpressionResolverImpl addKey(FieldExpression fieldExpression) {
        funcMap.put(fieldExpression, new NoOpsFieldExpressionHolderFuncImpl());
        return this;
    }

    private static class NoOpsFieldExpressionHolderFuncImpl implements FieldExpressionHolderFunc {
        @Override
        public String get(ParamsBuilder paramsBuilder) {
            throw new NoOpsFieldExpressionHolderFuncException("Should be replaced by real FieldExpressionHolderFuncImpl");
        }
    }
}