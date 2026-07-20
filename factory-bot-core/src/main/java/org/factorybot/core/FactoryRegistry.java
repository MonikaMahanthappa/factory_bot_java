package org.factorybot.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Holds factory instances (lazily created from their class) and their built {@link Definition}s. */
final class FactoryRegistry {

    private final Map<Class<?>, Factory<?>> instances = new ConcurrentHashMap<>();
    private final Map<Class<?>, Definition<?>> definitions = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    <T> Factory<T> get(Class<? extends Factory<T>> factoryClass) {
        return (Factory<T>) instances.computeIfAbsent(factoryClass, this::instantiate);
    }

    Factory<?> getRaw(Class<? extends Factory<?>> factoryClass) {
        return instances.computeIfAbsent(factoryClass, this::instantiate);
    }

    @SuppressWarnings("unchecked")
    <T> Definition<T> definitionOf(Factory<T> factory) {
        return (Definition<T>) definitions.computeIfAbsent(factory.getClass(), key -> factory.buildDefinition());
    }

    void register(Factory<?> factory) {
        instances.put(factory.getClass(), factory);
    }

    void clear() {
        instances.clear();
        definitions.clear();
    }

    private Factory<?> instantiate(Class<?> factoryClass) {
        try {
            var constructor = factoryClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (Factory<?>) constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new FactoryBotException("Could not instantiate factory " + factoryClass.getName()
                    + ". It needs a public no-arg constructor, or register an instance via FactoryBot.register(...).", e);
        }
    }
}
