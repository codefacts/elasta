package elasta.composer.state.handlers;

import elasta.composer.MsgEnterEventHandlerP;
import elasta.core.flow.EnterEventHandlerP;

/**
 * Created by sohan on 5/12/2017.
 */
public interface UpdateStateHandlerBuilder extends StateHandlerBuilder {
    @Override
    MsgEnterEventHandlerP build();
}
