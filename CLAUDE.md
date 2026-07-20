# CLAUDE.md — factory_bot_java

Guidance for Claude Code (and contributors) working in this repository.

## What this is

A type-safe, factory_bot-style test-data library for **Java 21 + Spring Boot**, with first-class **Spring
Data JPA** persistence. A clean-room port of the core model of Ruby's
[factory_bot](https://github.com/thoughtbot/factory_bot). See [`README.md`](README.md) and
[`docs/`](docs/) for the full rationale, decision flow, and feature-parity map.

## Layout

- `factory-bot-core` — ORM-agnostic engine (depends only on Datafaker).
- `factory-bot-spring-data-jpa` — persistence adapter: the `create` strategy + Spring Boot auto-config.

## Build & test

```bash
./gradlew build   # both modules + all tests, Java 21 toolchain
```

A JDK 21 must be discoverable by Gradle. `.java-version` pins `21.0` for jenv users; if Gradle picks the
wrong JVM, set `JAVA_HOME` to a JDK 21 for the command.

## Maintenance rules

### ⚠️ Keep the feature log up to date

**Whenever a new feature is added, append an entry to [`docs/features.md`](docs/features.md)** (newest
first, below the `APPEND NEW FEATURES BELOW THIS LINE` marker). Each entry must include:

- the date (absolute, e.g. `2026-07-20`),
- a short feature name and what it does,
- the public API surface (method signatures / class names),
- the module and the key source + test files.

Do this in the **same change** that introduces the feature — a feature is not done until it is logged.
If the feature also changes factory_bot parity, update [`docs/feature-parity.md`](docs/feature-parity.md)
too (status column / new row).

### Decision records

Significant design decisions get an ADR under [`docs/decisions/`](docs/decisions/) (numbered, dated) and a
line in [`docs/discussion-log.md`](docs/discussion-log.md).

## Conventions

- Public API lives in `org.factorybot.core` / `org.factorybot.springdata`; keep engine internals
  package-private.
- Every new feature ships with a test (core: `FactoryBotCoreTest`; JPA: `@DataJpaTest`).
- Prefer the type-safe method-reference DSL; keep the String-keyed overloads only as escape hatches
  (e.g. transient attributes).
