# ADR-0006: Build tooling — Gradle (Kotlin DSL), Java 21, multi-module, standalone repo

- **Date:** 2026-07-20
- **Status:** Accepted

## Context

Scaffolding choices for the new project, settled in a second diverge-mode round.

## Decisions

1. **Standalone project + its own git repo.** `factory_bot_java` lives in a sibling directory to the Ruby
   repo with an independent `git init` and no dependency on it. (Rationale: it is a separate product with a
   separate release cadence and audience.)
2. **Gradle with the Kotlin DSL** (`build.gradle.kts`). Modern default for new Java/Spring libraries; good
   multi-module support.
3. **Java 21 (LTS)** as the language baseline, via a Gradle toolchain (so the build is JDK-independent as
   long as a JDK 21 is discoverable). A `.java-version` pins `21.0` for jenv users.
4. **Multi-module layout:**
   - `factory-bot-core` — the ORM-agnostic engine (depends only on Datafaker).
   - `factory-bot-spring-data-jpa` — the persistence adapter (depends on core + Spring Data JPA).
   This mirrors factory_bot's core/`to_create` separation: build-only users never pull Spring/JPA.

## Consequences

- Contributors need a JDK 21 available to Gradle. The repo ships a Gradle wrapper (`./gradlew`) pinned to
  Gradle 9.6.1.
- The module split makes the persistence seam a physical boundary, not just a logical one.
- Kotlin-DSL build files are slightly less familiar to some enterprise Maven shops; accepted for the
  modern tooling benefit. A Maven consumer can still depend on the published artifacts.
