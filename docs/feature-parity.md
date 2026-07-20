# Feature parity: factory_bot → factory_bot_java

Maps each Ruby factory_bot concept to its Java equivalent, with the source-of-truth Ruby file and the
current MVP status. Last updated **2026-07-20**.

Legend: ✅ implemented · 🟡 partial · ⬜ planned

| factory_bot concept | Ruby source | factory_bot_java | Status |
|---|---|---|---|
| `FactoryBot.define` / `factory :user` | `syntax/default.rb` | `Factory<T>` subclass with `define(Definition<T>)`, referenced by class | ✅ |
| Class inference | `factory.rb#build_class` | Explicit `super(User.class)` (generics-erased at runtime) | ✅ |
| Static / dynamic attributes | `attribute/dynamic.rb` | `f.attr(User::setX, () -> ...)` | ✅ |
| Dependent attributes | `evaluator.rb` | `f.attr(User::setEmail, ev -> ev.get(User::getFirstName) + ...)` | ✅ |
| Inline sequences | `sequence.rb`, `definition_proxy.rb` | `f.sequence(User::setLogin, n -> "user" + n)` | ✅ |
| Global sequences | `sequence.rb`, `syntax/default.rb` | `FactoryBot.sequence("x", n -> ...)` + `FactoryBot.generate("x")` | ✅ |
| Associations | `attribute/association.rb` | `f.association(User::setAccount, AccountFactory.class, ...traits/overrides)` | ✅ |
| Association parent-strategy cascade | `strategy/*.rb`, `evaluator.rb` | `Strategy.associationStrategy()` (create→create, build→build, stub→stub) | ✅ |
| Traits (definition + call-time) | `trait.rb`, `definition.rb` | `f.trait("admin", t -> ...)`, `build(F.class, "admin")`; last-wins | ✅ |
| Base traits (`traits: [...]`) | `definition.rb` | `f.withTraits("...")` | ✅ |
| Transient attributes | `definition_proxy.rb#transient` | `f.transientAttr("postCount", 0)`; visible to callbacks, never assigned | ✅ |
| `build` | `strategy/build.rb` | `FactoryBot.build(F.class)` | ✅ |
| `create` | `strategy/create.rb`, `configuration.rb` | `FactoryBot.create(F.class)` via `PersistenceHandler` (JPA adapter) | ✅ |
| `build_stubbed` | `strategy/stub.rb` | `FactoryBot.buildStubbed(F.class)` — builds, no persist | 🟡 (no id/timestamp faking yet) |
| `attributes_for` | `strategy/attributes_for.rb` | `FactoryBot.attributesFor(F.class)` → `Map`, associations omitted | ✅ |
| `*_list` / `*_pair` | `strategy_syntax_method_registrar.rb` | `buildList` / `createList` / `buildStubbedList` / `buildPair` / `createPair` | ✅ |
| Callbacks (after build/create/stub, before create) | `callback.rb` | `afterBuild` / `beforeCreate` / `afterCreate` / `afterStub` (`BiConsumer<T,Evaluator<T>>`) | ✅ |
| Inheritance / nested factories | `factory.rb#compile` | `f.parent(ParentFactory.class)` — merges parent attrs/callbacks | ✅ |
| Call-time overrides | `evaluator.rb` | `Attributes.set(User::setX, v).and(...)` (typed) or `set("name", v)` (transient) | ✅ |
| `to_create` / persistence seam | `configuration.rb`, `evaluation.rb` | `PersistenceHandler` SPI; default build-only; JPA adapter persists | ✅ |
| Datafaker / faker integration | (java-faker in Ruby world) | `faker()` in factories; seedable via `FactoryBot.setFaker(...)` | ✅ |
| Factory aliases | `factory.rb#names` | — | ⬜ |
| FK alias interruption (`x` ↔ `x_id`) | `aliases.rb` | — | ⬜ |
| Linting (`FactoryBot.lint`) | `linter.rb` | — | ⬜ |
| Enum traits | `enum.rb` | — | ⬜ |
| Definition-file autoloading | `find_definitions.rb` | N/A — factories are classes; Spring component-scan planned | ⬜ |

## MVP scope delivered (2026-07-20)

- **Milestone 1 — core engine (build-only):** every ✅ row above except `create`/persistence. 15 tests.
- **Milestone 2 — Spring Data JPA adapter:** `create`, `PersistenceHandler`, `SpringDataPersistenceHandler`,
  auto-configuration, association-persistence cascade, `@DataJpaTest` transaction integration. 3 tests.

## Planned (future milestones)

- Linting, factory aliases + FK interruption, `build_stubbed` id/timestamp faking, enum traits,
  classpath factory discovery, Maven Central publication.
