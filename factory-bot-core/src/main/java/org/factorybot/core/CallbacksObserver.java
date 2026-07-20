package org.factorybot.core;

import java.util.List;

/** Fires the callbacks registered for a given lifecycle phase, in declaration order. */
final class CallbacksObserver<T> {

    private final List<Callback<T>> callbacks;

    CallbacksObserver(List<Callback<T>> callbacks) {
        this.callbacks = callbacks;
    }

    void fire(Callback.Phase phase, T instance, Evaluator<T> evaluator) {
        for (Callback<T> callback : callbacks) {
            if (callback.phase == phase) {
                callback.run(instance, evaluator);
            }
        }
    }
}
