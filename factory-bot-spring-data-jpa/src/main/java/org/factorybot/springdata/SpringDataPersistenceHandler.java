package org.factorybot.springdata;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.factorybot.core.PersistenceHandler;

/**
 * Persists factory-built entities via a JPA {@link EntityManager} — the Spring analogue of
 * factory_bot's default {@code to_create(&:save!)}. See ADR-0004.
 *
 * <p>The {@link EntityManager} is injected as a {@code @PersistenceContext} proxy, so it transparently
 * joins the caller's active transaction. The {@code create} strategy calls {@link #persist(Object)} once
 * the entity (and its associations, created first by the parent-strategy cascade) have been built. Under
 * a Spring {@code @DataJpaTest} the surrounding transaction rolls back, so rows never leak between tests.
 */
public final class SpringDataPersistenceHandler implements PersistenceHandler {

    @PersistenceContext
    private EntityManager entityManager;

    public SpringDataPersistenceHandler() {
    }

    /** For manual wiring / unit tests. */
    public SpringDataPersistenceHandler(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public <T> T persist(T instance) {
        entityManager.persist(instance);
        return instance;
    }
}
