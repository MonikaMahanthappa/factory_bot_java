package org.factorybot.core;

/**
 * Base class for a factory. Subclass it, pass the product type to {@code super(...)}, and describe the
 * factory in {@link #define(Definition)} using the type-safe fluent DSL.
 *
 * <pre>{@code
 * public class UserFactory extends Factory<User> {
 *     public UserFactory() { super(User.class); }
 *
 *     @Override
 *     protected void define(Definition<User> f) {
 *         f.attr(User::setFirstName, () -> faker().name().firstName());
 *         f.attr(User::setLastName,  () -> faker().name().lastName());
 *         f.attr(User::setEmail, ev ->
 *             ev.get(User::getFirstName) + "." + ev.get(User::getLastName) + "@example.com");
 *         f.trait("admin", t -> t.attr(User::setRole, () -> Role.ADMIN));
 *     }
 * }
 * }</pre>
 *
 * <p>Factories are looked up by their class (e.g. {@code FactoryBot.build(UserFactory.class)}); each is
 * lazily instantiated once via its public no-arg constructor and cached.
 */
public abstract class Factory<T> {

    private final Class<T> type;

    protected Factory(Class<T> type) {
        this.type = type;
    }

    /** The product type this factory builds. */
    public final Class<T> type() {
        return type;
    }

    /** Describe the factory here using {@code f.attr(...)}, {@code f.trait(...)}, etc. */
    protected abstract void define(Definition<T> f);

    /** Convenience accessor for the shared Datafaker instance, usable inside {@link #define}. */
    protected final net.datafaker.Faker faker() {
        return FactoryBot.sharedFaker();
    }

    /** Internal: runs {@link #define} against a fresh {@link Definition}. */
    final Definition<T> buildDefinition() {
        Definition<T> definition = new Definition<>(type, false);
        define(definition);
        return definition;
    }
}
