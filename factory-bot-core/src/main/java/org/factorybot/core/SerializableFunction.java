package org.factorybot.core;

import java.io.Serializable;
import java.util.function.Function;

/**
 * A {@link Function} that is {@link Serializable}, so that a getter method reference such as
 * {@code User::getName} can be introspected to recover the property name {@code "name"}.
 *
 * <p>Used by {@link Evaluator#get(SerializableFunction)} to let a dependent attribute reference
 * another attribute's already-resolved value with full type-safety and no string keys.
 */
@FunctionalInterface
public interface SerializableFunction<T, R> extends Function<T, R>, Serializable {
}
