# blog-app — example Spring Boot app using factory_bot_java

A small but realistic Spring Boot application whose **tests use [factory_bot_java](../../README.md) as the
test-data factory**. It exists to show the library from a consumer's perspective (package `com.example.blog`)
across the full test pyramid.

This module lives inside the factory_bot_java repo and depends on the library via `project(...)` references,
so it always builds against the current source and doubles as an end-to-end integration test.

## The domain & workflow

A blogging service with real state transitions and business rules:

- **Authors** write **Articles**, which start as `DRAFT`.
- An article is **published** (`DRAFT → PUBLISHED`, stamping `publishedAt`).
- Readers may **comment** — but **only on published articles** (commenting on a draft is rejected).

### REST endpoints (`ArticleController`)

| Method & path | Purpose |
|---|---|
| `POST /authors` | create an author |
| `POST /articles` | create a draft (slug derived from the title) |
| `POST /articles/{id}/publish` | publish a draft → `409` if already published |
| `POST /articles/{id}/comments` | comment → `409` if the article is still a draft |
| `GET /articles?status=PUBLISHED` | list by status |
| `GET /authors/{id}/articles` | an author's published articles |

## How the tests use factory_bot_java

Factories live under [`src/test/java/com/example/blog/factories`](src/test/java/com/example/blog/factories):

- **`ArticleFactory`** — a title **sequence**, a **dependent** slug (derived from the title), a faker body,
  an **association** to `AuthorFactory`, and a **`"published"` trait**.
- **`AuthorFactory`** — faker name + a unique email **sequence**.
- **`CommentFactory`** — associates a **published** article (so comments are valid by default).

Tests then arrange state with one line instead of hand-built fixtures, at three layers:

| Test | Slice | What it shows |
|---|---|---|
| `ArticleRepositoryTest` | `@DataJpaTest` | `create(ArticleFactory, "published")`, `createList`, trait + association-persistence cascade, derived queries |
| `ArticleServiceTest` | `@SpringBootTest` | factories arrange a draft / published article to exercise `publish()` and the comment-only-when-published rule |
| `ArticleControllerTest` | `@SpringBootTest` + MockMvc | factories seed the DB, then real HTTP endpoints are asserted |

All three extend `AbstractPostgresTest`, which starts **Postgres via Testcontainers** (`@ServiceConnection`)
and re-installs factory_bot_java's persistence handler after each `FactoryBot.reset()`.

## Running

```bash
# Run the app locally on embedded H2 (no Docker needed):
../../gradlew :examples:blog-app:bootRun

# Then, e.g.:
curl -XPOST localhost:8080/authors  -H 'Content-Type: application/json' -d '{"name":"Ada","email":"ada@example.com"}'
curl -XPOST localhost:8080/articles -H 'Content-Type: application/json' -d '{"authorId":1,"title":"Hello","body":"..."}'
curl -XPOST localhost:8080/articles/1/publish
curl      'localhost:8080/articles?status=PUBLISHED'

# Run the tests (requires Docker for Testcontainers Postgres):
../../gradlew :examples:blog-app:test
```
