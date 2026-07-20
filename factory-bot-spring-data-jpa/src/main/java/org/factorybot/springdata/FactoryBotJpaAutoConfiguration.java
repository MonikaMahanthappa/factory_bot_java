package org.factorybot.springdata;

import jakarta.persistence.EntityManagerFactory;
import org.factorybot.core.FactoryBot;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration that wires factory_bot_java's {@code create} strategy to JPA.
 *
 * <p>It registers a {@link SpringDataPersistenceHandler} (whose {@code EntityManager} is injected as a
 * transaction-aware {@code @PersistenceContext} proxy) and installs it on the global {@link FactoryBot},
 * so {@code FactoryBot.create(...)} persists. Present automatically once
 * {@code factory-bot-spring-data-jpa} is on the classpath.
 */
@AutoConfiguration
@ConditionalOnClass(EntityManagerFactory.class)
public class FactoryBotJpaAutoConfiguration {

    @Bean
    public SpringDataPersistenceHandler factoryBotPersistenceHandler() {
        SpringDataPersistenceHandler handler = new SpringDataPersistenceHandler();
        FactoryBot.setPersistenceHandler(handler);
        return handler;
    }
}
