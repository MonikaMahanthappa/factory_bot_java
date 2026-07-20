# ADR-0002: Language and type model — Java classes + builders/Lombok

- **Date:** 2026-07-20
- **Status:** Accepted

## Context

The DSL design depends heavily on the target language and object model. Options weighed:

- **Java (classes + builders/Lombok)** — widest Spring Boot audience; works with POJOs, Lombok `@Builder`,
  and JPA entities.
- **Kotlin DSL** — closest to factory_bot's expressive Ruby feel (lambdas, named args, trait blocks), but
  a narrower audience.
- **Java records first** — elegant for immutable data, but record immutability complicates lazy/dependent
  attributes and mutation-style callbacks.
- **Java + Kotlin both** — best reach, most work.

## Decision

Target **Java with classes + builders/Lombok** first.

## Consequences

- Attributes are assigned via setters (or builder methods), which pairs naturally with the type-safe
  method-reference DSL in [ADR-0003](0003-type-safe-fluent-dsl.md).
- Mutable POJOs/JPA entities are the primary model; records are not a first-class target in the MVP
  (they can be supported later via constructor-based construction and the record-accessor handling already
  present in the property resolver).
- Kotlin users can still consume the Java API; a dedicated Kotlin DSL is a possible future layer.
