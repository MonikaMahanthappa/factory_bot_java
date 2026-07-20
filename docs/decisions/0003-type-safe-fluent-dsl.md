# ADR-0003: Type-safe fluent DSL via method-reference property resolution

- **Date:** 2026-07-20
- **Status:** Accepted

## Context

factory_bot uses Ruby's dynamic DSL, where an attribute both names itself and references other attributes
by method name inside a block. Java needs a concrete, statically-typed equivalent. Options weighed:

1. **Type-safe fluent builder** — `f.attr(User::setName, () -> ...)`. Compile-time safety, refactor-safe.
2. **String-keyed map DSL** — `f.attr("name", () -> ...)`. Closest to factory_bot's feel, but stringly-typed.
3. **Annotation-driven** — declarative but rigid; poor fit for lazy/dependent attributes and callbacks.

The user chose **(1) type-safe fluent builder**.

## The core tension

A pure type-safe API assigns via setter method references — but factory_bot's most valuable trick is a
*dependent* attribute reading **other** attributes' resolved values. With method references, `User::setName`
and `User::getName` are distinct lambda objects; nothing links them at runtime unless we recover the
property they target.

## Decision

Derive the property name from the **method reference itself** using `SerializedLambda` introspection:

- Our functional interfaces (`SerializableBiConsumer`, `SerializableFunction`) extend `Serializable`.
- `PropertyResolver` calls the lambda's synthetic `writeReplace()` to get a `SerializedLambda`, reads
  `getImplMethodName()` (`"setFirstName"` / `"getFirstName"` / `"isActive"` / record accessor `"firstName"`),
  and strips the accessor prefix → `"firstName"` / `"active"`.
- Results are cached keyed by the lambda's class; each method-reference call site in source maps to a
  stable synthesized class, so the reflection cost is paid **once per site**.

This gives the best of both: `f.attr(User::setName, ...)` is fully type-safe *and* auto-names the attribute,
so `ev.get(User::getFirstName)` resolves the same key with **zero string keys**. A String-keyed escape hatch
(`f.transientAttr("postCount", ...)`, `Attributes.set("postCount", v)`) remains for transient attributes,
which have no setter/property.

## Consequences

- **Pros:** compile-time safety, refactor-safe attribute references, no magic strings for the common path,
  and record accessors work for free.
- **Cons / risks:**
  - Depends on `SerializedLambda` behavior — a lambda passed where a method reference is expected cannot be
    introspected; we throw a clear error directing the user to a method reference or the String overload.
  - Small first-use reflection cost per call site (mitigated by the class-keyed cache).
- **Alternatives kept in reserve:** explicit-name overloads (`f.attr("name", setter, value)`) already exist,
  so users who dislike the lambda introspection can bypass it entirely.
