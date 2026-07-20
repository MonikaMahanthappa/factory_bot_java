# factory_bot_java

A type-safe, factory_bot-style test-data library for **Java 21 + Spring Boot**, with first-class
**Spring Data JPA** persistence.

> Status: **MVP / proof of concept** (v0.1.0-SNAPSHOT). Built as a clean-room port of the core model of
> Ruby's [factory_bot](https://github.com/thoughtbot/factory_bot). See [`docs/`](docs/) for the full
> research, decision flow, and feature-parity map.

## Why this exists

Spring Boot ships no test-data factory. The Java ecosystem splits into two camps and neither is a live,
idiomatic factory_bot:

- **Random-data generators** — [Instancio](https://www.instancio.org/), EasyRandom, Podam. Great, but
  *random-first*: no named factories with domain-meaningful defaults, traits, or associations as
  first-class concepts.
- **factory_bot ports** — [java-factory-bot](https://github.com/topicusoverheid/java-factory-bot) is the
  closest in spirit but is unmaintained Groovy (v0.2.0, no releases) with no Spring Data/JPA integration;
  [fixture-factory](https://github.com/six2six/fixture-factory) and
  [Beanmother](https://github.com/keepcosmos/beanmother) are older template/YAML tools.

The gap: an actively-maintained, **type-safe** library with factory_bot's declarative model *and*
first-class JPA persistence. Full analysis in [ADR-0001](docs/decisions/0001-why-build-this.md).

## Quick look

```java
public class UserFactory extends Factory<User> {
    public UserFactory() { super(User.class); }

    @Override
    protected void define(Definition<User> f) {
        f.attr(User::setFirstName, () -> faker().name().firstName());
        f.attr(User::setLastName,  () -> faker().name().lastName());

        // Dependent attribute — reads other attributes, fully type-safe, no string keys:
        f.attr(User::setEmail, ev ->
            (ev.get(User::getFirstName) + "." + ev.get(User::getLastName) + "@example.com").toLowerCase());

        f.sequence(User::setLogin, n -> "user" + n);          // inline sequence
        f.association(User::setAccount, AccountFactory.class); // parent-strategy cascade
        f.trait("admin", t -> t.attr(User::setRole, () -> Role.ADMIN));
    }
}
```

```java
User u        = FactoryBot.build(UserFactory.class);                       // in-memory
User admin    = FactoryBot.create(UserFactory.class, "admin");             // persisted via JPA
User jane     = FactoryBot.build(UserFactory.class,
                    Attributes.set(User::setFirstName, "Jane"));           // typed overrides
List<User> us = FactoryBot.buildList(UserFactory.class, 3);
Map<String,Object> attrs = FactoryBot.attributesFor(UserFactory.class);
```

## Modules

| Module | What it is | Depends on |
|---|---|---|
| `factory-bot-core` | ORM-agnostic engine: factories, attributes, sequences, traits, transient attrs, associations, callbacks, `build`/`buildStubbed`/`attributesFor`. | Datafaker only |
| `factory-bot-spring-data-jpa` | Persistence adapter: the `create` strategy + Spring Boot auto-configuration that persists via JPA. | core + Spring Data JPA |

## Build & test

```bash
./gradlew build     # compiles both modules, runs all tests on the Java 21 toolchain
```

Requires a JDK 21 available to Gradle (the repo pins `.java-version` to `21.0` for jenv users).

## Design decisions

All decisions were made in an explicit "diverge mode" discussion — captured, with timestamps, in:

- [`docs/features.md`](docs/features.md) — running feature log (append-on-add)
- [`docs/discussion-log.md`](docs/discussion-log.md) — the chronological research & Q&A flow
- [`docs/decisions/`](docs/decisions/) — one ADR per major fork
- [`docs/feature-parity.md`](docs/feature-parity.md) — factory_bot → factory_bot_java feature map

## License

MIT (intended).
