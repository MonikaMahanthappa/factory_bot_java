package com.example.blog.support;

import org.factorybot.core.FactoryBot;
import org.factorybot.springdata.SpringDataPersistenceHandler;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base for the example's integration tests: a real Postgres via Testcontainers (auto-wired into Spring
 * Boot by {@code @ServiceConnection}), plus per-test factory_bot_java reset.
 *
 * <p>{@code FactoryBot.reset()} clears the global persistence handler, so we re-install the
 * Spring-managed one before each test — the same pattern the library's own JPA tests use.
 */
@Testcontainers
public abstract class AbstractPostgresTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private SpringDataPersistenceHandler persistenceHandler;

    @BeforeEach
    void resetFactoryBot() {
        FactoryBot.reset();
        FactoryBot.setPersistenceHandler(persistenceHandler);
    }
}
