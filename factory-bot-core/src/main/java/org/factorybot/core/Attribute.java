package org.factorybot.core;

import java.util.function.Function;

/**
 * A single resolved-at-build-time attribute of a factory.
 *
 * <p>Unifies factory_bot's static/dynamic/dependent attributes, sequences and associations behind one
 * shape: a {@code resolver} that produces the value (given the {@link Evaluator}) and an optional
 * {@code setter} that assigns it onto the product. Transient attributes have no setter and are never
 * assigned, but remain resolvable by dependent attributes and callbacks.
 */
final class Attribute<T> {

    enum Kind { VALUE, ASSOCIATION }

    final String name;
    final boolean isTransient;
    final Kind kind;
    private final SerializableBiConsumer<T, Object> setter; // nullable
    private final Function<Evaluator<T>, Object> resolver;

    private Attribute(String name, boolean isTransient, Kind kind,
                      SerializableBiConsumer<T, Object> setter, Function<Evaluator<T>, Object> resolver) {
        this.name = name;
        this.isTransient = isTransient;
        this.kind = kind;
        this.setter = setter;
        this.resolver = resolver;
    }

    static <T> Attribute<T> value(String name, SerializableBiConsumer<T, Object> setter,
                                  Function<Evaluator<T>, Object> resolver) {
        return new Attribute<>(name, false, Kind.VALUE, setter, resolver);
    }

    static <T> Attribute<T> transientValue(String name, Function<Evaluator<T>, Object> resolver) {
        return new Attribute<>(name, true, Kind.VALUE, null, resolver);
    }

    static <T> Attribute<T> association(String name, SerializableBiConsumer<T, Object> setter,
                                        Function<Evaluator<T>, Object> resolver) {
        return new Attribute<>(name, false, Kind.ASSOCIATION, setter, resolver);
    }

    Object resolve(Evaluator<T> evaluator) {
        return resolver.apply(evaluator);
    }

    void assign(T instance, Object value) {
        if (!isTransient && setter != null) {
            setter.accept(instance, value);
        }
    }
}
