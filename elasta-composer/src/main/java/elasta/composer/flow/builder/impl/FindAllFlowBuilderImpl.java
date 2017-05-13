package elasta.composer.flow.builder.impl;

import elasta.composer.Events;
import elasta.composer.States;
import elasta.composer.flow.builder.FindAllFlowBuilder;
import elasta.core.flow.EnterEventHandler;
import elasta.core.flow.EnterEventHandlerP;
import elasta.core.flow.Flow;

import java.util.Objects;

/**
 * Created by sohan on 5/12/2017.
 */
final public class FindAllFlowBuilderImpl implements FindAllFlowBuilder {
    final EnterEventHandlerP startHandler;
    final EnterEventHandlerP authorizationHandler;
    final EnterEventHandlerP findAllHandler;
    final EnterEventHandlerP endHandler;

    public FindAllFlowBuilderImpl(EnterEventHandlerP startHandler, EnterEventHandlerP authorizationHandler, EnterEventHandlerP findAllHandler, EnterEventHandlerP endHandler) {
        Objects.requireNonNull(startHandler);
        Objects.requireNonNull(authorizationHandler);
        Objects.requireNonNull(findAllHandler);
        Objects.requireNonNull(endHandler);
        this.startHandler = startHandler;
        this.authorizationHandler = authorizationHandler;
        this.findAllHandler = findAllHandler;
        this.endHandler = endHandler;
    }

    @Override
    public Flow build() {
        return Flow.builder()
            .when(States.start, Flow.on(Events.next, States.authorization))
            .when(
                States.authorization,
                Flow.on(Events.next, States.findAll),
                Flow.on(Events.authorizationError, States.end)
            )
            .when(States.findAll, Flow.on(Events.next, States.end))
            .when(States.end, Flow.end())
            .handlersP(States.start, startHandler)
            .handlersP(States.authorization, authorizationHandler)
            .handlersP(States.findAll, findAllHandler)
            .handlersP(States.end, endHandler)
            .build();
    }
}
