package elasta.core.flow;

import elasta.core.intfs.Fun1Unckd;
import elasta.core.promise.intfs.Promise;

import java.util.concurrent.Callable;

public class FlowCallbacksBuilder<T, R> {
    private Fun1Unckd<T, Promise<FlowTrigger<R>>> onEnter;
    private Callable<Promise<Void>> onExit;

    public FlowCallbacksBuilder() {
    }

    public FlowCallbacksBuilder<T, R> onEnter(Fun1Unckd<T, Promise<FlowTrigger<R>>> onEnter) {
        this.onEnter = onEnter;
        return this;
    }

    public FlowCallbacksBuilder<T, R> onExit(Callable<Promise<Void>> onExit) {
        this.onExit = onExit;
        return this;
    }

    public FlowCallbacks<T, R> build() {
        return new FlowCallbacks<>(onEnter, onExit);
    }

    public static <T, R> FlowCallbacksBuilder<T, R> create() {
        return new FlowCallbacksBuilder<>();
    }
}