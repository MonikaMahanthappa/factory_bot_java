package org.factorybot.core;

import org.factorybot.core.Callback.Phase;

/**
 * The build strategies, mirroring factory_bot's {@code build} / {@code create} / {@code build_stubbed}.
 * Each also declares the strategy used to build its associations, giving factory_bot's parent-strategy
 * cascade (create→create, build→build, buildStubbed→buildStubbed).
 *
 * <p>{@code attributesFor} is not a {@code Strategy} here because it yields a {@code Map}, not a product;
 * it is handled directly by {@link FactoryBot}.
 */
enum Strategy {

    /** Instantiate and assign attributes; run after-build callbacks. No persistence. */
    BUILD {
        @Override
        <T> T run(Evaluation<T> evaluation) {
            T instance = evaluation.instantiateAndAssign();
            evaluation.callbacks.fire(Phase.AFTER_BUILD, instance, evaluation.evaluator);
            return instance;
        }

        @Override
        Strategy associationStrategy() {
            return BUILD;
        }
    },

    /** Build, then persist via the configured {@link PersistenceHandler}; run before/after-create callbacks. */
    CREATE {
        @Override
        <T> T run(Evaluation<T> evaluation) {
            T instance = evaluation.instantiateAndAssign();
            evaluation.callbacks.fire(Phase.AFTER_BUILD, instance, evaluation.evaluator);
            evaluation.callbacks.fire(Phase.BEFORE_CREATE, instance, evaluation.evaluator);
            T persisted = evaluation.evaluator.engine().persistence().persist(instance);
            evaluation.callbacks.fire(Phase.AFTER_CREATE, persisted, evaluation.evaluator);
            return persisted;
        }

        @Override
        Strategy associationStrategy() {
            return CREATE;
        }
    },

    /** Build without persisting; run after-stub callbacks. Associations are likewise stubbed. */
    STUB {
        @Override
        <T> T run(Evaluation<T> evaluation) {
            T instance = evaluation.instantiateAndAssign();
            evaluation.callbacks.fire(Phase.AFTER_STUB, instance, evaluation.evaluator);
            return instance;
        }

        @Override
        Strategy associationStrategy() {
            return STUB;
        }
    };

    abstract <T> T run(Evaluation<T> evaluation);

    abstract Strategy associationStrategy();
}
