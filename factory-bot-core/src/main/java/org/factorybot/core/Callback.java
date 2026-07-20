package org.factorybot.core;

import java.util.function.BiConsumer;

/** A lifecycle hook, mirroring factory_bot's {@code after(:build)} / {@code before(:create)} etc. */
final class Callback<T> {

    enum Phase { AFTER_BUILD, BEFORE_CREATE, AFTER_CREATE, AFTER_STUB }

    final Phase phase;
    private final BiConsumer<T, Evaluator<T>> action;

    Callback(Phase phase, BiConsumer<T, Evaluator<T>> action) {
        this.phase = phase;
        this.action = action;
    }

    void run(T instance, Evaluator<T> evaluator) {
        action.accept(instance, evaluator);
    }
}
