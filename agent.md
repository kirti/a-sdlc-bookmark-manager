# Agent Rules: Bookmark Manager

> This file is persistent and cumulative. It is updated, never rewritten, by the
> sdlc-refinement skill's Stage 3. Every entry below has a source task ID so changes
> are traceable. Do not hand-edit — propose changes through the skill so they go
> through `merge_agent_md.py`.

<!-- MERGE:META
last_updated: 2026-07-04T20:02:01.437535+00:00
last_updated_by_task: cicd-github-actions
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
<!-- /MERGE:SECTION -->

## Architecture Rules

<!-- MERGE:SECTION architecture_rules -->
- New modules must not introduce a new HTTP client library if one is already in use in the repo. _(source: bookmark-manager)_
- Config and secrets via environment variables only — never hardcoded, even in examples. _(source: bookmark-manager)_
- All error output goes to stderr with a non-zero exit code; all successful output goes to stdout. Never mix the two streams. _(source: bookmark-manager-crud)_
- CI workflow has two jobs: `test` (runs on every push/PR) and `release` (runs on `v*.*.*` tags only, needs `test`). Never merge these into one job. _(source: cicd-github-actions)_
- The `release` job must declare `permissions: contents: write` explicitly and only on that job — do not grant write permissions to the whole workflow. _(source: cicd-github-actions)_
- Use the built-in `GITHUB_TOKEN` for release creation — never introduce a PAT or extra secret for operations the built-in token already covers. _(source: cicd-github-actions)_
<!-- /MERGE:SECTION -->

## Testing Rules

<!-- MERGE:SECTION testing_rules -->
- Use `kotlin.test` or JUnit5 with `@Test`; one assertion concern per test method. _(source: bookmark-manager)_
- Do not hit real external APIs or network resources in unit tests. _(source: bookmark-manager)_
<!-- /MERGE:SECTION -->

## Naming Conventions

<!-- MERGE:SECTION naming_conventions -->
- `PascalCase` for classes, `camelCase` for functions/properties, `UPPER_SNAKE_CASE` for top-level constants. _(source: bookmark-manager)_
<!-- /MERGE:SECTION -->

## Superseded Rules (history — do not delete)

<!-- MERGE:SECTION superseded -->

<!-- /MERGE:SECTION -->
