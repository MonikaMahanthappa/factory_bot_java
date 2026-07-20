package org.factorybot.core;

import java.io.Serializable;
import java.util.function.BiConsumer;

/**
 * A {@link BiConsumer} that is {@link Serializable}, so that a method reference such as
 * {@code User::setName} can be introspected at runtime to recover the property name it targets.
 *
 * <p>This is what makes the factory DSL type-safe <em>and</em> free of string property keys:
 * {@code attr(User::setName, ...)} both assigns via the setter and derives the attribute name
 * {@code "name"} (see {@link PropertyResolver}). See ADR-0003.
 */
@FunctionalInterface
public interface SerializableBiConsumer<T, U> extends BiConsumer<T, U>, Serializable {
}
