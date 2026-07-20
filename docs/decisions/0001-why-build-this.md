# ADR-0001: Why build factory_bot_java (validated hunch)

- **Date:** 2026-07-20
- **Status:** Accepted

## Context

The user (a Ruby factory_bot maintainer) hypothesized that Java + Spring Boot lacks a good way to create
test data, and asked to validate that before building anything ("no assumptions; validate first").

We surveyed the Java test-data ecosystem:

- **Random-data generators** — [Instancio](https://www.instancio.org/) (modern, maintained), EasyRandom,
  Podam. These populate objects with *random* values. Philosophically *random-first*: they do not center
  on named factories with domain-meaningful defaults, traits, and associations the way factory_bot does.
- **factory_bot ports** — [java-factory-bot](https://github.com/topicusoverheid/java-factory-bot) is the
  closest in spirit but is **Groovy, v0.2.0, no published releases, effectively unmaintained**, and has no
  Spring Data/JPA integration. [fixture-factory](https://github.com/six2six/fixture-factory) and
  [Beanmother](https://github.com/keepcosmos/beanmother) are older template/YAML-based tools.
- **Fake-value libraries** — Datafaker / JavaFaker generate values, not object graphs.
- **Default Spring guidance** — hand-rolled Object Mother + Builder patterns, `@Sql` scripts, `@DataJpaTest`.

## Decision

Build a new library. The validated gap: **there is no actively-maintained, type-safe Java/Spring Boot
library offering factory_bot's declarative model (named factories + traits + associations + sequences +
transient attributes + callbacks + build/create/stub strategies) together with first-class Spring Data JPA
persistence.**

## Consequences

- We are not competing with Instancio on random generation; we occupy the *declarative factory* niche and
  delegate fake values to Datafaker (see [ADR-0005](0005-clean-room-core-datafaker.md)).
- factory_bot's ORM-agnostic core + single persistence seam (`to_create`) is a clean blueprint to port;
  the seam becomes a JPA adapter (see [ADR-0004](0004-first-class-spring-data-jpa.md)).
- Hunch confirmed **with nuance**: libraries exist, but none fills this specific niche while being alive
  and idiomatic.
