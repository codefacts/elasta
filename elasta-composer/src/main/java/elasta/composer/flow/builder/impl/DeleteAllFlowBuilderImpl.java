package elasta.composer.flow.builder.impl;

import elasta.composer.Events;
import elasta.composer.MsgEnterEventHandlerP;
import elasta.composer.States;
import elasta.composer.flow.builder.DeleteAllFlowBuilder;
import elasta.core.flow.Flow;

import java.util.Objects;

/**
 * Created by sohan on 5/20/2017.
 */
final public class DeleteAllFlowBuilderImpl implements DeleteAllFlowBuilder {
    final MsgEnterEventHandlerP startHandler;
    final MsgEnterEventHandlerP authorizeAllHandler;
    final MsgEnterEventHandlerP deleteAllHandler;
    final MsgEnterEventHandlerP broadcastAllHandler;
    final MsgEnterEventHandlerP generateResponseHandler;
    final MsgEnterEventHandlerP endHandler;

    public DeleteAllFlowBuilderImpl(MsgEnterEventHandlerP startHandler, MsgEnterEventHandlerP authorizeAllHandler, MsgEnterEventHandlerP deleteAllHandler, MsgEnterEventHandlerP broadcastAllHandler, MsgEnterEventHandlerP generateResponseHandler, MsgEnterEventHandlerP endHandler) {
        Objects.requireNonNull(startHandler);
        Objects.requireNonNull(authorizeAllHandler);
        Objects.requireNonNull(deleteAllHandler);
        Objects.requireNonNull(broadcastAllHandler);
        Objects.requireNonNull(generateResponseHandler);
        Objects.requireNonNull(endHandler);
        this.startHandler = startHandler;
        this.authorizeAllHandler = authorizeAllHandler;
        this.deleteAllHandler = deleteAllHandler;
        this.broadcastAllHandler = broadcastAllHandler;
        this.generateResponseHandler = generateResponseHandler;
        this.endHandler = endHandler;
    }

    @Override
    public Flow build() {
        return Flow.builder()
            .when(States.start, Flow.on(Events.next, States.authorizeAll))
            .when(
                States.authorizeAll,
                Flow.on(Events.next, States.deleteAll),
                Flow.on(Events.authorizationError, States.end)
            )
            .when(States.deleteAll, Flow.on(Events.next, States.broadcastAll))
            .when(States.broadcastAll, Flow.on(Events.next, States.generateResponse))
            .when(States.generateResponse, Flow.on(Events.next, States.end))
            .when(States.end, Flow.end())
            .handlersP(States.start, startHandler)
            .handlersP(States.authorizeAll, authorizeAllHandler)
            .handlersP(States.deleteAll, deleteAllHandler)
            .handlersP(States.broadcastAll, broadcastAllHandler)
            .handlersP(States.generateResponse, generateResponseHandler)
            .handlersP(States.end, endHandler)
            .initialState(States.start)
            .build();
    }
}
