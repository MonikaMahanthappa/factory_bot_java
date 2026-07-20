package org.factorybot.core;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Type-safe, call-time attribute overrides — the Java analogue of factory_bot's
 * {@code create(:user, name: "Jane")}.
 *
 * <pre>{@code
 * User u = FactoryBot.build(UserFactory.class,
 *     Attributes.<User>set(User::setFirstName, "Jane").and(User::setLastName, "Doe"));
 * }</pre>
 *
 * <p>Property names are recovered from the setter method references, so overrides stay refactor-safe.
 * A String-keyed {@link #set(String, Object)} escape hatch exists for transient attributes (which
 * have no setter).
 */
public final class Attributes<T> {

    private final Map<String, Object> values = new LinkedHashMap<>();

    private Attributes() {
    }

    /** Begins a set of overrides with one typed setter/value pair. */
    public static <T, V> Attributes<T> set(SerializableBiConsumer<T, V> setter, V value) {
        return new Attributes<T>().and(setter, value);
    }

    /** Begins a set of overrides with one String-keyed (e.g. transient) attribute. */
    public static <T> Attributes<T> set(String name, Object value) {
        return new Attributes<T>().and(name, value);
    }

    /** Adds another typed setter/value override. */
    public <V> Attributes<T> and(SerializableBiConsumer<T, V> setter, V value) {
        values.put(PropertyResolver.propertyName(setter), value);
        return this;
    }

    /** Adds another String-keyed override (for transient attributes without a setter). */
    public Attributes<T> and(String name, Object value) {
        values.put(name, value);
        return this;
    }

    Map<String, Object> asMap() {
        return values;
    }
}
