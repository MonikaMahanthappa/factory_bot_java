# Features

A running log of features in factory_bot_java, newest first. **Append a new entry every time a feature is
added** (see the maintenance rule in [`CLAUDE.md`](../CLAUDE.md)).

Each entry: date (absolute), a short feature name, what it does, the public API surface, and the module +
key source/test files. For a concept-by-concept map against Ruby factory_bot, see
[`feature-parity.md`](feature-parity.md).

<!-- APPEND NEW FEATURES BELOW THIS LINE (newest first) -->

---

## 2026-07-20 — Example Spring Boot app (`examples/blog-app`)

- **What:** A realistic full-stack Spring Boot blog app (authors → draft articles → publish → comments,
  with real business rules) whose tests use factory_bot_java as the test-data factory across the pyramid.
  Demonstrates the library from a consumer's perspective (`com.example.blog`). Not a library feature — an
  end-to-end usage example that doubles as an integration test.
- **Demonstrates:** sequences, dependent attributes, associations, traits, and `create`/`createList` with
  typed overrides, at `@DataJpaTest`, `@SpringBootTest` (service), and MockMvc (web) layers.
- **Module:** `examples/blog-app` — app under `src/main/java/com/example/blog/**`; factories under
  `src/test/java/com/example/blog/factories/**`; Testcontainers base `support/AbstractPostgresTest`.
- **Tests:** `ArticleRepositoryTest` (2), `ArticleServiceTest` (3), `ArticleControllerTest` (2) — Postgres
  via Testcontainers. Runs locally on H2 via `bootRun`.

---

## 2026-07-20 — Spring Data JPA `create` persistence

- **What:** The `create` strategy persists built entities through a `PersistenceHandler` SPI backed by a
  JPA `EntityManager`; a Spring Boot auto-configuration installs it automatically. Association persistence
  cascades (children persisted before parents).
- **API:** `FactoryBot.create(F.class, ...)`, `FactoryBot.createList(...)`, `FactoryBot.createPair(...)`;
  `PersistenceHandler` (core SPI); `SpringDataPersistenceHandler`; `FactoryBotJpaAutoConfiguration`.
- **Module:** `factory-bot-spring-data-jpa` —
  `SpringDataPersistenceHandler.java`, `FactoryBotJpaAutoConfiguration.java`,
  `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
- **Tests:** `CreateStrategyJpaTest` (`@DataJpaTest`) — persistence, association cascade, build-only, rollback isolation.

## 2026-07-20 — Core engine (ORM-agnostic, build-only)

Initial MVP of the clean-room factory engine. Individual capabilities:

- **Factory definition & registration** — `Factory<T>` subclass with `define(Definition<T>)`, referenced by
  class and lazily instantiated. (`Factory.java`, `FactoryRegistry.java`)
- **Type-safe fluent DSL** — attribute names recovered from setter/getter method references via
  `SerializedLambda`, so no string keys. (`Definition.java`, `PropertyResolver.java`,
  `SerializableBiConsumer`/`SerializableFunction`)
- **Attributes** — static/dynamic (`attr(User::setX, () -> ...)`) and dependent
  (`attr(User::setEmail, ev -> ev.get(User::getFirstName) + ...)`). (`Attribute.java`, `Evaluator.java`)
- **Sequences** — inline (`f.sequence(User::setLogin, n -> "user" + n)`) and global
  (`FactoryBot.sequence(name, fn)` / `FactoryBot.generate(name)`). (`Sequence.java`)
- **Associations** — `f.association(User::setAccount, AccountFactory.class, ...)` with parent-strategy
  cascade (create→create, build→build, stub→stub). (`Strategy.java`, `Evaluator#buildAssociation`)
- **Traits** — `f.trait("admin", t -> ...)`, applied at call time (`build(F.class, "admin")`), last-wins;
  base traits via `f.withTraits(...)`. (`FactoryCompiler.java`)
- **Transient attributes** — `f.transientAttr("postCount", 0)`; visible to callbacks/dependents, never assigned.
- **Strategies** — `build`, `buildStubbed`, `attributesFor` (associations omitted from the map);
  `*List` / `*Pair` variants. (`Strategy.java`, `FactoryBot.java`)
- **Callbacks** — `afterBuild` / `beforeCreate` / `afterCreate` / `afterStub`
  (`BiConsumer<T, Evaluator<T>>`). (`Callback.java`, `CallbacksObserver.java`)
- **Inheritance** — `f.parent(ParentFactory.class)` merges parent attributes/callbacks. (`FactoryCompiler.java`)
- **Typed call-time overrides** — `Attributes.set(User::setX, v).and(...)`. (`Attributes.java`)
- **Datafaker integration** — `faker()` inside factories; seedable via `FactoryBot.setFaker(...)`.
- **Module:** `factory-bot-core`. **Tests:** `FactoryBotCoreTest` (15 feature tests).
