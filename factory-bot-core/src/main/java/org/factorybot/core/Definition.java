package org.factorybot.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.Supplier;

/**
 * The type-safe fluent builder handed to {@link Factory#define(Definition)} (and, in a restricted form,
 * to {@code trait} bodies). Every method returns {@code this} for chaining.
 *
 * <p>Attributes are keyed by the property name recovered from the setter method reference, so a later
 * declaration (or a trait, or a call-time override) with the same name wins — matching factory_bot.
 */
public final class Definition<T> {

    private final Class<T> type;
    private final boolean traitContext;

    private Supplier<T> constructor;
    private Class<? extends Factory<?>> parentClass;
    private final List<String> baseTraits = new ArrayList<>();
    private final List<Attribute<T>> attributes = new ArrayList<>();
    private final Map<String, Consumer<Definition<T>>> traits = new LinkedHashMap<>();
    private final List<Callback<T>> callbacks = new ArrayList<>();

    Definition(Class<T> type, boolean traitContext) {
        this.type = type;
        this.traitContext = traitContext;
    }

    // ------------------------------------------------------------------ construction / inheritance

    /** Customize how the product is instantiated (default: public no-arg constructor via reflection). */
    public Definition<T> construct(Supplier<T> constructor) {
        guardFactoryOnly("construct");
        this.constructor = constructor;
        return this;
    }

    /** Inherit attributes, callbacks and constructor from a parent factory (factory_bot inheritance). */
    public Definition<T> parent(Class<? extends Factory<?>> parent) {
        guardFactoryOnly("parent");
        this.parentClass = parent;
        return this;
    }

    /** Traits always applied to this factory (factory_bot's {@code traits: [...]}). */
    public Definition<T> withTraits(String... traitNames) {
        guardFactoryOnly("withTraits");
        for (String name : traitNames) {
            baseTraits.add(name);
        }
        return this;
    }

    // ------------------------------------------------------------------ attributes

    /** A static or lazily-computed attribute: {@code attr(User::setName, () -> faker().name().name())}. */
    public <V> Definition<T> attr(SerializableBiConsumer<T, V> setter, Supplier<V> value) {
        String name = PropertyResolver.propertyName(setter);
        attributes.add(Attribute.value(name, erase(setter), ev -> value.get()));
        return this;
    }

    /** A dependent attribute that reads other attributes via the evaluator. */
    public <V> Definition<T> attr(SerializableBiConsumer<T, V> setter, Function<Evaluator<T>, V> value) {
        String name = PropertyResolver.propertyName(setter);
        attributes.add(Attribute.value(name, erase(setter), ev -> value.apply(ev)));
        return this;
    }

    /** An attribute with an explicit name (rarely needed; the setter normally supplies the name). */
    public <V> Definition<T> attr(String name, SerializableBiConsumer<T, V> setter, Supplier<V> value) {
        attributes.add(Attribute.value(name, erase(setter), ev -> value.get()));
        return this;
    }

    // ------------------------------------------------------------------ sequences

    /** An inline, factory-scoped sequence: {@code sequence(User::setLogin, n -> "user" + n)} (starts at 1). */
    public <V> Definition<T> sequence(SerializableBiConsumer<T, V> setter, LongFunction<V> formatter) {
        return sequence(setter, 1L, formatter);
    }

    /** An inline sequence with an explicit starting value. */
    public <V> Definition<T> sequence(SerializableBiConsumer<T, V> setter, long start, LongFunction<V> formatter) {
        String name = PropertyResolver.propertyName(setter);
        Sequence<V> sequence = new Sequence<>(start, formatter);
        attributes.add(Attribute.value(name, erase(setter), ev -> sequence.next()));
        return this;
    }

    // ------------------------------------------------------------------ associations

    /**
     * An association to another factory. The child is built with the same strategy as the parent by
     * default (create→create, build→build, buildStubbed→buildStubbed). Extra {@code String} arguments
     * are traits and {@link Attributes} arguments are overrides applied to the child.
     */
    public <A> Definition<T> association(SerializableBiConsumer<T, A> setter,
                                         Class<? extends Factory<A>> factory,
                                         Object... traitsAndOverrides) {
        String name = PropertyResolver.propertyName(setter);
        List<String> traitNames = new ArrayList<>();
        Map<String, Object> overrides = new LinkedHashMap<>();
        for (Object arg : traitsAndOverrides) {
            if (arg instanceof String traitName) {
                traitNames.add(traitName);
            } else if (arg instanceof Attributes<?> attrs) {
                overrides.putAll(attrs.asMap());
            } else {
                throw new FactoryBotException("association(...) arguments must be trait names (String) or "
                        + "Attributes overrides, but got: " + arg);
            }
        }
        Function<Evaluator<T>, Object> resolver =
                ev -> ev.buildAssociation(factory, traitNames, overrides);
        attributes.add(Attribute.association(name, erase(setter), resolver));
        return this;
    }

    // ------------------------------------------------------------------ transient attributes

    /** A transient attribute holding a fixed value — visible to callbacks/dependents, never assigned. */
    public <V> Definition<T> transientAttr(String name, V value) {
        attributes.add(Attribute.transientValue(name, ev -> value));
        return this;
    }

    /** A transient attribute computed lazily. */
    public <V> Definition<T> transientAttr(String name, Supplier<V> value) {
        attributes.add(Attribute.transientValue(name, ev -> value.get()));
        return this;
    }

    // ------------------------------------------------------------------ traits

    /** Define a named trait — a reusable bundle of attributes/associations/callbacks. */
    public Definition<T> trait(String name, Consumer<Definition<T>> body) {
        guardFactoryOnly("trait");
        traits.put(name, body);
        return this;
    }

    // ------------------------------------------------------------------ callbacks

    public Definition<T> afterBuild(BiConsumer<T, Evaluator<T>> action) {
        callbacks.add(new Callback<>(Callback.Phase.AFTER_BUILD, action));
        return this;
    }

    public Definition<T> afterBuild(Consumer<T> action) {
        return afterBuild((instance, ev) -> action.accept(instance));
    }

    public Definition<T> beforeCreate(BiConsumer<T, Evaluator<T>> action) {
        callbacks.add(new Callback<>(Callback.Phase.BEFORE_CREATE, action));
        return this;
    }

    public Definition<T> beforeCreate(Consumer<T> action) {
        return beforeCreate((instance, ev) -> action.accept(instance));
    }

    public Definition<T> afterCreate(BiConsumer<T, Evaluator<T>> action) {
        callbacks.add(new Callback<>(Callback.Phase.AFTER_CREATE, action));
        return this;
    }

    public Definition<T> afterCreate(Consumer<T> action) {
        return afterCreate((instance, ev) -> action.accept(instance));
    }

    public Definition<T> afterStub(BiConsumer<T, Evaluator<T>> action) {
        callbacks.add(new Callback<>(Callback.Phase.AFTER_STUB, action));
        return this;
    }

    public Definition<T> afterStub(Consumer<T> action) {
        return afterStub((instance, ev) -> action.accept(instance));
    }

    // ------------------------------------------------------------------ internal accessors

    Class<T> type() {
        return type;
    }

    Supplier<T> constructor() {
        return constructor;
    }

    Class<? extends Factory<?>> parentClass() {
        return parentClass;
    }

    List<String> baseTraits() {
        return baseTraits;
    }

    List<Attribute<T>> attributes() {
        return attributes;
    }

    Map<String, Consumer<Definition<T>>> traits() {
        return traits;
    }

    List<Callback<T>> callbacks() {
        return callbacks;
    }

    private void guardFactoryOnly(String method) {
        if (traitContext) {
            throw new FactoryBotException(method + "(...) is not allowed inside a trait body.");
        }
    }

    @SuppressWarnings("unchecked")
    private static <T, V> SerializableBiConsumer<T, Object> erase(SerializableBiConsumer<T, V> setter) {
        return (SerializableBiConsumer<T, Object>) setter;
    }
}
