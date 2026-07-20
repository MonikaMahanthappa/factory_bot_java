# ADR-0004: First-class Spring Data JPA persistence via an SPI seam

- **Date:** 2026-07-20
- **Status:** Accepted

## Context

factory_bot's `create` persists by calling `save!` — its one ActiveRecord assumption, deliberately made
overridable through `to_create`. The user chose **first-class Spring Data JPA** as the persistence target
(over "build-only first" or a fully generic pluggable SPI as the headline feature).

## Decision

- Define a **`PersistenceHandler` SPI** in `factory-bot-core` — the Java analogue of `to_create`:
  `<T> T persist(T instance)`. Core ships only `PersistenceHandler.BUILD_ONLY`, which throws a clear
  `NoPersistenceConfiguredException` — so `build`/`buildStubbed`/`attributesFor` work with zero persistence
  dependencies, and `create` fails loudly until an adapter is present.
- Ship a **`factory-bot-spring-data-jpa`** module with `SpringDataPersistenceHandler`, which persists via a
  JPA `EntityManager` injected as a transaction-aware `@PersistenceContext` proxy, plus a Spring Boot
  **auto-configuration** that installs it on the global `FactoryBot` automatically.
- The `create` strategy's association cascade (create→create) persists associations **before** their parent,
  satisfying foreign-key ordering.

## Why EntityManager (not repository lookup)

Using `EntityManager.persist` is entity-type-agnostic and needs no per-entity repository wiring; Spring
Data's `save` ultimately does the same. The `@PersistenceContext` proxy transparently joins the caller's
transaction, so under `@DataJpaTest` all writes roll back — verified by the adapter's integration tests
(`count() == 0` at the start of each test).

## Consequences

- Keeps `factory-bot-core` free of any Spring/JPA dependency — build-only users pull nothing extra.
- The SPI leaves the door open for other adapters later (MyBatis, jOOQ, MongoDB) without touching core —
  a lightweight version of the "pluggable persistence" option, without paying its up-front cost now.
- The global-singleton `FactoryBot` means the auto-config sets a process-wide handler; tests that call
  `FactoryBot.reset()` must re-install it (the adapter test does so in `@BeforeEach`).
