# ADR-0005: Clean-room core, Datafaker for fake values

- **Date:** 2026-07-20
- **Status:** Accepted

## Context

Two ways to get a factory engine: build the factory/trait/association/strategy model ourselves, or layer a
factory_bot-style API over an existing library (e.g. Instancio). Options weighed:

1. **Clean-room core + Datafaker for values** — own the model; delegate only fake-value generation.
2. **Wrap Instancio** — reuse its reflection/population engine, but inherit its random-first model.
3. **Pure clean-room** — implement fake values too (reinvent Faker).

The user chose **(1)**.

## Decision

- Implement the engine from scratch: `Factory`, `Definition`, `Attribute`, `Evaluator`, `Strategy`,
  `Sequence`, `PersistenceHandler`, `FactoryBot`. This gives full control over factory_bot's exact
  semantics (parent-strategy association cascade, last-wins traits, transient attributes, dependent
  attribute resolution) with no impedance mismatch.
- Depend on **[Datafaker](https://www.datafaker.net/)** solely for realistic fake values, exposed as
  `faker()` inside factories and swappable via `FactoryBot.setFaker(...)` (e.g. a seeded instance for
  reproducibility).

## Consequences

- **Pros:** no random-first baggage; the model matches factory_bot precisely; one small, focused
  dependency in core.
- **Cons:** we own more code than a wrapper would. Mitigated — the core is intentionally small and fully
  unit-tested (15 parity tests).
- Not wrapping Instancio does not preclude interop later; a user can still call Instancio inside an
  attribute supplier if they want random population for a specific field.
