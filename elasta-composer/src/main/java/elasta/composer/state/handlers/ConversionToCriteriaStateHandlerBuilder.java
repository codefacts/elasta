package elasta.composer.state.handlers;

import elasta.core.flow.EnterEventHandlerP;

/**
 * Created by sohan on 5/12/2017.
 */
public interface ConversionToCriteriaStateHandlerBuilder extends StateHandlerBuilder {
    @Override
    EnterEventHandlerP build();
}
