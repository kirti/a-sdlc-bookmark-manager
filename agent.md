# Agent Rules: Bookmark Manager

> This file is persistent and cumulative. It is updated, never rewritten, by the
> sdlc-refinement skill's Stage 3. Every entry below has a source task ID so changes
> are traceable. Do not hand-edit — propose changes through the skill so they go
> through `merge_agent_md.py`.

<!-- MERGE:META
last_updated: 2026-07-04T22:12:15.098323+00:00
last_updated_by_task: learning-2026-07
-->

## Coding Rules

<!-- MERGE:SECTION coding_rules -->
- Prefer data classes for DTOs; avoid mutable public fields. _(source: bookmark-manager)_
- Use sealed classes for finite state/result types instead of enums with payload fields. _(source: bookmark-manager)_
- Null-safety: avoid `!!`; prefer `?.let` or explicit early returns. _(source: bookmark-manager)_
- File writes that must survive crashes must use atomic temp-file-then-rename (write to a sibling `.tmp` file, then `Files.move` with `ATOMIC_MOVE`). _(source: bookmark-manager)_
- Use a sealed `Result` type (Ok/Err) for all store operations that can fail — never use exceptions for expected error paths (duplicate URL, not found). _(source: bookmark-manager-crud)_
- URL is the natural key for `BookmarkStore`; uniqueness is enforced at the store layer, not at the data-class level. _(source: bookmark-manager-crud)_
- Always `chmod +x gradlew` before any `./gradlew` invocation in CI — Linux runners do not preserve file permissions from git. _(source: cicd-github-actions)_
- When a resource key can contain URL-reserved characters (e.g. a bookmark URL), pass it in the request body or query string, never embedded in the path. _(source: bookmark-manager-http)_
- Emit structured single-line log records (fields: ts, component, op/method, result/status, latency_ms) for every store operation and HTTP request; never log full store-file contents. _(source: bookmark-manager-http)_
<!-- /MERGE:SECTION -->

## Architecture Rules

<!-- MERGE:SECTION architecture_rules -->
- New modules must not introduce a new HTTP client library if one is already in use in the repo. _(source: bookmark-manager)_
- Config and secrets via environment variables only — never hardcoded, even in examples. _(source: bookmark-manager)_
- All error output goes to stderr with a non-zero exit code; all successful output goes to stdout. Never mix the two streams. _(source: bookmark-manager-crud)_
- CI workflow has two jobs: `test` (runs on every push/PR) and `release` (runs on `v*.*.*` tags only, needs `test`). Never merge these into one job. _(source: cicd-github-actions)_
- The `release` job must declare `permissions: contents: write` explicitly and only on that job — do not grant write permissions to the whole workflow. _(source: cicd-github-actions)_
- Use the built-in `GITHUB_TOKEN` for release creation — never introduce a PAT or extra secret for operations the built-in token already covers. _(source: cicd-github-actions)_
- Every service exposes a thin Ktor (Netty) HTTP layer that wraps the domain store 1:1 and holds no business logic of its own. A `GET /health` endpoint is mandatory — the api-tester gate polls it before running the Postman suite. (The existing 'no new HTTP client library' rule concerns clients; a Ktor server is the standing server choice and does not conflict.) _(source: bookmark-manager-http)_
- HTTP status mapping is fixed: store `Err.DuplicateUrl` -> 409, `Err.NotFound` -> 404, malformed/missing request body -> 400, store IO/parse failure -> 500. _(source: bookmark-manager-http)_
- CI `test` job runs unit tests AND the Newman API suite (start the fat JAR's ServerKt on 127.0.0.1, poll GET /health, then `newman run postman/collection.json`). The `release` job (tags only, needs test) builds and pushes the Docker image to `ghcr.io/<owner>/<repo>` (lowercased) using the built-in GITHUB_TOKEN with `packages: write`, and triggers deploy only via a gated Deploy-Hook step after all tests pass — never the deploy platform's own auto-deploy-on-push. _(source: cicd-http)_
<!-- /MERGE:SECTION -->

## Testing Rules

<!-- MERGE:SECTION testing_rules -->
- Use `kotlin.test` or JUnit5 with `@Test`; one assertion concern per test method. _(source: bookmark-manager)_
- Do not hit real external APIs or network resources in unit tests. _(source: bookmark-manager)_
- Every HTTP endpoint must have a matching Postman v2.1 request with a status-code assertion, runnable via Newman (the api-tester gate) — including the negative cases (409 duplicate, 404 not-found). _(source: bookmark-manager-http)_
- Before the pre-push gate, every acceptance criterion and every edge case listed in the task's requirements.md must have at least one corresponding test. A stated criterion or edge case shipped without a test is a gate failure, not a follow-up. (Stage 10 learning: this pattern caused two of three round-1 code-review rejections on bookmark-manager-http.) _(source: learning-2026-07)_
<!-- /MERGE:SECTION -->

## Naming Conventions

<!-- MERGE:SECTION naming_conventions -->
- `PascalCase` for classes, `camelCase` for functions/properties, `UPPER_SNAKE_CASE` for top-level constants. _(source: bookmark-manager)_
<!-- /MERGE:SECTION -->

## Superseded Rules (history — do not delete)

<!-- MERGE:SECTION superseded -->

<!-- /MERGE:SECTION -->
