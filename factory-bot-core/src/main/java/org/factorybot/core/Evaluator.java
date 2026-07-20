package org.factorybot.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The per-build resolution context. Resolves attribute values lazily and memoizes them, so dependent
 * attributes and callbacks all observe one consistent set of values. Handed to dynamic attributes
 * ({@code ev -> ...}) and callbacks.
 */
public final class Evaluator<T> {

    private final CompiledFactory<T> compiled;
    private final Map<String, Object> overrides;
    private final Strategy strategy;
    private final Engine engine;

    private final Map<String, Object> cache = new HashMap<>();
    private final Set<String> resolving = new HashSet<>();

    Evaluator(CompiledFactory<T> compiled, Map<String, Object> overrides, Strategy strategy, Engine engine) {
        this.compiled = compiled;
        this.overrides = overrides;
        this.strategy = strategy;
        this.engine = engine;
    }

    /** Resolve (and memoize) an attribute by name. Call-time overrides win over the factory's default. */
    public Object get(String name) {
        if (cache.containsKey(name)) {
            return cache.get(name);
        }
        if (overrides.containsKey(name)) {
            Object value = overrides.get(name);
            cache.put(name, value);
            return value;
        }
        Attribute<T> attribute = compiled.attribute(name);
        if (attribute == null) {
            throw new FactoryBotException("No attribute '" + name + "' is defined on factory for "
                    + compiled.type.getName() + ".");
        }
        if (!resolving.add(name)) {
            throw new CircularAttributeDependencyException(name);
        }
        try {
            Object value = attribute.resolve(this);
            cache.put(name, value);
            return value;
        } finally {
            resolving.remove(name);
        }
    }

    /** Resolve another attribute by its getter reference, with the value's static type: {@code ev.get(User::getName)}. */
    @SuppressWarnings("unchecked")
    public <V> V get(SerializableFunction<T, V> getter) {
        return (V) get(PropertyResolver.propertyName(getter));
    }

    /** Resolve an attribute by name with an expected type (handy for transient attributes). */
    @SuppressWarnings("unchecked")
    public <V> V get(String name, Class<V> type) {
        return (V) get(name);
    }

    /** The shared Datafaker instance. */
    public net.datafaker.Faker faker() {
        return engine.faker();
    }

    <A> A buildAssociation(Class<? extends Factory<A>> factory, List<String> traits, Map<String, Object> overrides) {
        return engine.run(factory, strategy.associationStrategy(), overrides, traits);
    }

    Engine engine() {
        return engine;
    }
}
