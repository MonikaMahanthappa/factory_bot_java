package org.factorybot.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Flattens a factory into a {@link CompiledFactory} for one build: it merges the parent chain, the
 * factory's own declarations, its base traits, and the call-time traits — later declarations override
 * earlier ones by attribute name (factory_bot semantics).
 */
final class FactoryCompiler {

    private final FactoryRegistry registry;

    FactoryCompiler(FactoryRegistry registry) {
        this.registry = registry;
    }

    <T> CompiledFactory<T> compile(Factory<T> factory, List<String> callTimeTraits) {
        Definition<T> definition = registry.definitionOf(factory);

        LinkedHashMap<String, Attribute<T>> attributes = new LinkedHashMap<>();
        List<Callback<T>> callbacks = new ArrayList<>();

        // 1. Inherit from parent (attributes + callbacks only; the product type is this factory's own).
        if (definition.parentClass() != null) {
            CompiledFactory<T> parent = compileParent(definition.parentClass());
            attributes.putAll(parent.attributes);
            callbacks.addAll(parent.callbacks);
        }

        // 2. This factory's own declarations.
        for (Attribute<T> attribute : definition.attributes()) {
            attributes.put(attribute.name, attribute);
        }
        callbacks.addAll(definition.callbacks());

        // 3. Base traits, then call-time traits (later wins).
        List<String> traitsToApply = new ArrayList<>(definition.baseTraits());
        traitsToApply.addAll(callTimeTraits);
        for (String traitName : traitsToApply) {
            applyTrait(definition, traitName, attributes, callbacks);
        }

        Supplier<T> constructor = definition.constructor() != null
                ? definition.constructor()
                : reflectiveConstructor(definition.type());

        return new CompiledFactory<>(definition.type(), constructor, attributes, callbacks);
    }

    @SuppressWarnings("unchecked")
    private <T> CompiledFactory<T> compileParent(Class<? extends Factory<?>> parentClass) {
        Factory<?> parent = registry.getRaw(parentClass);
        // The parent's setters target a supertype of T, so its attributes/callbacks apply to a T instance.
        return (CompiledFactory<T>) compile(parent, List.of());
    }

    private <T> void applyTrait(Definition<T> definition, String traitName,
                                LinkedHashMap<String, Attribute<T>> attributes, List<Callback<T>> callbacks) {
        Consumer<Definition<T>> body = definition.traits().get(traitName);
        if (body == null) {
            throw new FactoryBotException("Unknown trait '" + traitName + "' for factory "
                    + definition.type().getName() + ".");
        }
        Definition<T> traitDefinition = new Definition<>(definition.type(), true);
        body.accept(traitDefinition);
        for (Attribute<T> attribute : traitDefinition.attributes()) {
            attributes.put(attribute.name, attribute);
        }
        callbacks.addAll(traitDefinition.callbacks());
    }

    private static <T> Supplier<T> reflectiveConstructor(Class<T> type) {
        return () -> {
            try {
                var constructor = type.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new FactoryBotException("Could not instantiate " + type.getName()
                        + " via a no-arg constructor. Add one, or use construct(...) in the factory.", e);
            }
        };
    }
}
