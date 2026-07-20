package org.factorybot.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongFunction;

/**
 * The entry point — the Java analogue of Ruby's {@code FactoryBot} module.
 *
 * <pre>{@code
 * User u        = FactoryBot.build(UserFactory.class);
 * User admin    = FactoryBot.create(UserFactory.class, "admin");
 * User jane     = FactoryBot.build(UserFactory.class, Attributes.<User>set(User::setFirstName, "Jane"));
 * List<User> us = FactoryBot.buildList(UserFactory.class, 3);
 * Map<String,Object> attrs = FactoryBot.attributesFor(UserFactory.class);
 * }</pre>
 *
 * <p>Factories are referenced by class and lazily instantiated. {@code create} requires a
 * {@link PersistenceHandler} (supplied by {@code factory-bot-spring-data-jpa}); {@code build} does not.
 */
public final class FactoryBot implements Engine {

    private static final FactoryBot INSTANCE = new FactoryBot();

    private final FactoryRegistry registry = new FactoryRegistry();
    private final FactoryCompiler compiler = new FactoryCompiler(registry);
    private final Map<String, Sequence<?>> globalSequences = new ConcurrentHashMap<>();
    private volatile net.datafaker.Faker faker = new net.datafaker.Faker();
    private volatile PersistenceHandler persistenceHandler = PersistenceHandler.BUILD_ONLY;

    private FactoryBot() {
    }

    // ------------------------------------------------------------------ configuration

    /** Register a factory instance explicitly (otherwise factories are lazily created from their class). */
    public static void register(Factory<?> factory) {
        INSTANCE.registry.register(factory);
    }

    /** Replace the shared Datafaker instance (e.g. a seeded one for reproducible values). */
    public static void setFaker(net.datafaker.Faker faker) {
        INSTANCE.faker = faker;
    }

    /** Install the persistence handler used by {@code create} (done for you by the Spring Data JPA adapter). */
    public static void setPersistenceHandler(PersistenceHandler handler) {
        INSTANCE.persistenceHandler = handler;
    }

    /** Reset all registered factories, sequences, the faker and persistence handler (useful between tests). */
    public static void reset() {
        INSTANCE.registry.clear();
        INSTANCE.globalSequences.clear();
        INSTANCE.faker = new net.datafaker.Faker();
        INSTANCE.persistenceHandler = PersistenceHandler.BUILD_ONLY;
    }

    /** The shared Datafaker instance, for use inside {@link Factory#faker()}. */
    static net.datafaker.Faker sharedFaker() {
        return INSTANCE.faker;
    }

    // ------------------------------------------------------------------ build / create / stub

    public static <T> T build(Class<? extends Factory<T>> factory, Object... args) {
        return INSTANCE.run(factory, Strategy.BUILD, overridesOf(args), traitsOf(args));
    }

    public static <T> T create(Class<? extends Factory<T>> factory, Object... args) {
        return INSTANCE.run(factory, Strategy.CREATE, overridesOf(args), traitsOf(args));
    }

    public static <T> T buildStubbed(Class<? extends Factory<T>> factory, Object... args) {
        return INSTANCE.run(factory, Strategy.STUB, overridesOf(args), traitsOf(args));
    }

    public static <T> Map<String, Object> attributesFor(Class<? extends Factory<T>> factory, Object... args) {
        return INSTANCE.attributesForImpl(factory, overridesOf(args), traitsOf(args));
    }

    // ------------------------------------------------------------------ list / pair helpers

    public static <T> List<T> buildList(Class<? extends Factory<T>> factory, int count, Object... args) {
        return repeat(count, () -> build(factory, args));
    }

    public static <T> List<T> createList(Class<? extends Factory<T>> factory, int count, Object... args) {
        return repeat(count, () -> create(factory, args));
    }

    public static <T> List<T> buildStubbedList(Class<? extends Factory<T>> factory, int count, Object... args) {
        return repeat(count, () -> buildStubbed(factory, args));
    }

    public static <T> List<T> buildPair(Class<? extends Factory<T>> factory, Object... args) {
        return buildList(factory, 2, args);
    }

    public static <T> List<T> createPair(Class<? extends Factory<T>> factory, Object... args) {
        return createList(factory, 2, args);
    }

    // ------------------------------------------------------------------ global sequences

    /** Register a global sequence starting at 1: {@code FactoryBot.sequence("email", n -> "u" + n + "@x.com")}. */
    public static <V> void sequence(String name, LongFunction<V> formatter) {
        sequence(name, 1L, formatter);
    }

    /** Register a global sequence with an explicit starting value. */
    public static <V> void sequence(String name, long start, LongFunction<V> formatter) {
        INSTANCE.globalSequences.put(name, new Sequence<>(start, formatter));
    }

    /** Produce the next value of a previously-registered global sequence. */
    @SuppressWarnings("unchecked")
    public static <V> V generate(String name) {
        Sequence<V> sequence = (Sequence<V>) INSTANCE.globalSequences.get(name);
        if (sequence == null) {
            throw new FactoryBotException("No global sequence named '" + name + "' has been registered.");
        }
        return sequence.next();
    }

    // ------------------------------------------------------------------ Engine implementation

    @Override
    public <X> X run(Class<? extends Factory<X>> factoryClass, Strategy strategy,
                     Map<String, Object> overrides, List<String> traits) {
        Factory<X> factory = registry.get(factoryClass);
        CompiledFactory<X> compiled = compiler.compile(factory, traits);
        Evaluator<X> evaluator = new Evaluator<>(compiled, overrides, strategy, this);
        Evaluation<X> evaluation = new Evaluation<>(compiled, evaluator, new CallbacksObserver<>(compiled.callbacks));
        return strategy.run(evaluation);
    }

    @Override
    public net.datafaker.Faker faker() {
        return faker;
    }

    @Override
    public PersistenceHandler persistence() {
        return persistenceHandler;
    }

    private <T> Map<String, Object> attributesForImpl(Class<? extends Factory<T>> factoryClass,
                                                      Map<String, Object> overrides, List<String> traits) {
        Factory<T> factory = registry.get(factoryClass);
        CompiledFactory<T> compiled = compiler.compile(factory, traits);
        Evaluator<T> evaluator = new Evaluator<>(compiled, overrides, Strategy.BUILD, this);
        Evaluation<T> evaluation = new Evaluation<>(compiled, evaluator, new CallbacksObserver<>(compiled.callbacks));
        return evaluation.toHash();
    }

    // ------------------------------------------------------------------ argument parsing helpers

    private static Map<String, Object> overridesOf(Object[] args) {
        Map<String, Object> overrides = new LinkedHashMap<>();
        for (Object arg : args) {
            if (arg instanceof Attributes<?> attributes) {
                overrides.putAll(attributes.asMap());
            } else if (!(arg instanceof String)) {
                throw new FactoryBotException("Arguments must be trait names (String) or Attributes overrides, "
                        + "but got: " + arg);
            }
        }
        return overrides;
    }

    private static List<String> traitsOf(Object[] args) {
        List<String> traits = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof String traitName) {
                traits.add(traitName);
            }
        }
        return traits;
    }

    private static <T> List<T> repeat(int count, java.util.function.Supplier<T> supplier) {
        List<T> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(supplier.get());
        }
        return list;
    }
}
