# Discussion & Decision Log

A chronological record of the "diverge mode" discussion that produced factory_bot_java — the hunch, the
research that validated it, every question asked, and the answers given. Timestamps are in the project's
working timezone.

---

## 2026-07-20 — Origin

**Prompt (user, maintainer of Ruby factory_bot):** "This is a Ruby on Rails factories library. I want
the equivalent of this for Java + Spring Boot. Spring Boot does not have a good way to create test data.
Or is there another library or pattern? I want to verify my hunch. Then, if my hunch is correct, migrate
the feature here for a Spring Boot–compatible library. Diverge mode. Ask questions. Validate. No
assumptions. Suggest options. Create a new factory_bot_java project and put all the discussion and
decision flow [in it]."

Mode agreed: **diverge / validate-first**. No assumptions; ask before building.

---

## 2026-07-20 — Step 1: Understand what factory_bot actually is

Produced a complete feature inventory of the Ruby gem (factories, static/dynamic/dependent attributes,
sequences, associations with parent-strategy cascade, traits, transient attributes, the
build/create/build_stubbed/attributes_for strategy abstraction, callbacks, inheritance, aliases,
`to_create`/`skip_create` persistence seam, linting, configuration/loading).

**Key finding:** the engine is ORM-agnostic *except one seam* — the default `to_create(&:save!)` in
`configuration.rb`. Everything routes through `to_create` / `initialize_with` / strategy objects. **That
seam is what a Java port parameterizes with a JPA adapter.** → captured in
[feature-parity.md](feature-parity.md).

---

## 2026-07-20 — Step 2: Validate the hunch (research the Java/Spring ecosystem)

Web research across the Java test-data landscape. Findings:

| Tool | Verdict |
|---|---|
| [Instancio](https://www.instancio.org/) | Modern, actively maintained — but **random-first**, not declarative-factory-first. Different philosophy. |
| EasyRandom, Podam | Random population; EasyRandom less actively developed. |
| [java-factory-bot](https://github.com/topicusoverheid/java-factory-bot) | Closest factory_bot port in spirit — but **Groovy, v0.2.0, no releases, unmaintained**, no Spring Data/JPA. |
| [fixture-factory (six2six)](https://github.com/six2six/fixture-factory) | Template-based, older; has processors + sequences, not idiomatic modern Spring Boot. |
| [Beanmother](https://github.com/keepcosmos/beanmother) | YAML fixtures / ObjectMother; not type-safe DSL. |
| Datafaker / JavaFaker | Fake *values*, not object factories. |
| Spring default advice | Hand-rolled [Object Mother + Builder](https://reflectoring.io/objectmother-fluent-builder/), `@Sql`, `@DataJpaTest` — no library. |

**Conclusion — hunch CONFIRMED, with nuance:** Spring Boot has no factory library; the closest
factory_bot port is dead; the popular modern option (Instancio) is philosophically different. The real
gap is *an actively-maintained, type-safe library with factory_bot's declarative model **and**
first-class Spring Data JPA persistence.* → [ADR-0001](decisions/0001-why-build-this.md).

---

## 2026-07-20 — Step 3: Decisions (diverge-mode Q&A)

Four architecture-shaping questions were asked. Answers given by the user:

1. **Language / type model** → **Java (classes + builders/Lombok).** → [ADR-0002](decisions/0002-language-and-type-model.md)
2. **DSL style** → **Type-safe fluent builder.** → [ADR-0003](decisions/0003-type-safe-fluent-dsl.md)
3. **Persistence depth** → **First-class Spring Data JPA.** → [ADR-0004](decisions/0004-first-class-spring-data-jpa.md)
4. **Build vs wrap** → **Clean-room core; Datafaker for fake values only** (not wrapping Instancio). → [ADR-0005](decisions/0005-clean-room-core-datafaker.md)

A second round settled scaffolding:

5. **Build tool** → **Gradle (Kotlin DSL).**
6. **Java baseline** → **Java 21 (LTS).**
7. **Project location** → **Standalone separate folder + its own git repo** (`factory_bot_java`, sibling
   to the Ruby repo; independent `git init`, no dependency on it).
8. **Module layout** → **Multi-module: `factory-bot-core` (ORM-agnostic) + `factory-bot-spring-data-jpa`
   (adapter).**

5–8 captured in [ADR-0006](decisions/0006-build-java21-gradle-multimodule.md).

---

## 2026-07-20 — Step 4: Build the MVP

Implemented and verified a working vertical slice:

- **`factory-bot-core`** — the full ORM-agnostic engine + a 15-test parity suite (all green). Each test
  maps to a factory_bot feature: dependent attributes, inline & global sequences, call-time traits +
  last-wins precedence, transient attributes driving callbacks, typed overrides, association cascade,
  `buildStubbed`, `attributesFor` (associations omitted), inheritance, `buildList`, and a clear failure
  when `create` is called with no persistence configured.
- **`factory-bot-spring-data-jpa`** — the `create` strategy via a `PersistenceHandler` backed by a JPA
  `EntityManager`, plus Spring Boot auto-configuration. A 3-test `@DataJpaTest` suite proves `create`
  persists, cascades association persistence (author before book), that `build` stays in-memory, and that
  each test's writes roll back (the `count() == 0` assertions).

`./gradlew build` → **BUILD SUCCESSFUL**, 18/18 tests passing on the Java 21 toolchain.

### Notable implementation decision surfaced during the build

The type-safe DSL derives attribute names from setter/getter **method references** via `SerializedLambda`
introspection (`User::setName` → `"name"`), so dependent attributes read `ev.get(User::getFirstName)`
with zero string keys. Trade-off and alternatives recorded in
[ADR-0003](decisions/0003-type-safe-fluent-dsl.md).

---

## Open items / future milestones

- `FactoryBot.lint()` with rolled-back transaction (factory_bot's linter).
- `build_stubbed` id/timestamp faking in the JPA adapter (core currently leaves ids null).
- Foreign-key `x`/`x_id` alias interruption.
- Enum-driven traits.
- Publish to Maven Central; documentation site.
