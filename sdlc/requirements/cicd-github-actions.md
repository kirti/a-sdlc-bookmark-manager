# Requirements: cicd-github-actions

> Status: **DRAFT**
> Source: user request (2026-07-04)
> Generated: 2026-07-04

## Summary

A GitHub Actions CI/CD pipeline for the Bookmark Manager Kotlin project. On every push and pull request the pipeline runs the full test suite. On qualifying pushes (see Open Questions) it also builds a self-contained fat JAR and publishes it as a GitHub artifact.

## Acceptance Criteria

- [ ] A GitHub Actions workflow triggers on every push to any branch and on every pull request
- [ ] The workflow runs `./gradlew test` and fails the build if any test fails
- [ ] JDK 21 is used in the workflow environment (matching the project's `jvmToolchain(21)`)
- [ ] Gradle dependencies are cached between runs to avoid re-downloading on every execution
- [ ] On push of a `v*.*.*` git tag, the workflow builds a fat JAR containing all runtime dependencies
- [ ] The fat JAR is attached to a GitHub Release created for that tag, downloadable from the repository's Releases page
- [ ] Pushes to branches and pull requests run tests only and do not publish an artifact
- [ ] The `gradlew` script is made executable in CI before any Gradle commands run

## Edge Cases

- First pipeline run with no prior Gradle cache — must still succeed (cold-cache bootstrap)
- `gradlew` file permissions may not be executable after a fresh checkout on Linux runners — must `chmod +x gradlew` or equivalent
- Fat JAR build step must not run on pull requests from forks (no write token available)
- If tests fail, the fat JAR step must be skipped — never publish a broken artifact
- Pushing a `v*.*.*` tag on a commit that has failing tests must not produce a release — test job must be a required predecessor

## Non-Functional Requirements

- End-to-end pipeline (test + build + publish) should complete within 5 minutes on a warm cache
- No hardcoded secrets in the workflow file — use GitHub Actions secrets or the built-in `GITHUB_TOKEN`
- Workflow file must be valid YAML and pass `actionlint` logic (no syntax errors)

## Non-Goals

- Deployment to any runtime environment (this is build, test, and artifact publish only)
- Docker image build or container registry publish
- Notifications (Slack, email, etc.)
- Multi-platform or cross-compiled builds
- Code coverage reporting

## Open Questions

_None._

---
*Do not edit `status` manually — set to `APPROVED` only by merging the reviewing PR.*
