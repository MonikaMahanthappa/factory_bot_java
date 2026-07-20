package org.factorybot.core;

import java.util.LinkedHashMap;
import java.util.Map;

/** Bundles the compiled factory, its evaluator and callbacks for a single strategy run. */
final class Evaluation<T> {

    final CompiledFactory<T> compiled;
    final Evaluator<T> evaluator;
    final CallbacksObserver<T> callbacks;

    Evaluation(CompiledFactory<T> compiled, Evaluator<T> evaluator, CallbacksObserver<T> callbacks) {
        this.compiled = compiled;
        this.evaluator = evaluator;
        this.callbacks = callbacks;
    }

    /** Instantiate the product and assign every non-transient attribute. */
    T instantiateAndAssign() {
        T instance = compiled.constructor.get();
        for (Attribute<T> attribute : compiled.attributes.values()) {
            if (attribute.isTransient) {
                continue;
            }
            attribute.assign(instance, evaluator.get(attribute.name));
        }
        return instance;
    }

    /** Produce a name→value map of non-transient, non-association attributes (for {@code attributesFor}). */
    Map<String, Object> toHash() {
        Map<String, Object> hash = new LinkedHashMap<>();
        for (Attribute<T> attribute : compiled.attributes.values()) {
            if (attribute.isTransient || attribute.kind == Attribute.Kind.ASSOCIATION) {
                continue;
            }
            hash.put(attribute.name, evaluator.get(attribute.name));
        }
        return hash;
    }
}
