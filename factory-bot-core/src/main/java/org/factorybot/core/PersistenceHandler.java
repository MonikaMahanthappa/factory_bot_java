package org.factorybot.core;

/**
 * The seam that the {@code create} strategy uses to persist a built object — the Java analogue of
 * factory_bot's {@code to_create} (which defaults to {@code save!}). See ADR-0004.
 *
 * <p>The core module ships only {@link #BUILD_ONLY}, which refuses to persist. The
 * {@code factory-bot-spring-data-jpa} module supplies an adapter that saves via a Spring Data
 * repository / {@code EntityManager}.
 */
@FunctionalInterface
public interface PersistenceHandler {

    /**
     * Persists {@code instance} and returns the persisted entity (which may be a different instance,
     * e.g. the return value of {@code JpaRepository.save}).
     */
    <T> T persist(T instance);

    /** Default handler used by {@code factory-bot-core} alone: {@code create} is unavailable. */
    PersistenceHandler BUILD_ONLY = new PersistenceHandler() {
        @Override
        public <T> T persist(T instance) {
            throw new NoPersistenceConfiguredException();
        }
    };
}
