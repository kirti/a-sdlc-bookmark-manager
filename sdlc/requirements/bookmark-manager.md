# Requirements: bookmark-manager

> Status: **APPROVED** (human-approved 2026-07-04 via direct instruction; open questions resolved inline)
> Source: /Users/kirtikaushal/ai-skils/asdlc/kotlin-scaffold/sdlc/draft.md
> Generated: 2026-07-04

## Summary

A small internal tool that lets the team save bookmarks (a URL, a title, and an optional tag) and list or search them later, so useful internal links shared in chat stop getting lost. Bookmarks persist to a local JSON file so they survive between runs. Scope is deliberately small: a few hundred bookmarks, one person using it at a time. Kotlin/JVM per team default, starting from the existing `kotlin-scaffold`.

## Sources Consulted

| Type | Reference |
|---|---|
| User upload (draft) | `sdlc/draft.md` — Business Request: Bookmark Manager (internal tools team, Low priority) |
| Technical notes | Engineering notes within `sdlc/draft.md` — Kotlin/JVM default, local JSON storage, `kotlin-scaffold` starter |
| Scaffold registry | `references/scaffold_registry.md` — `kotlin-scaffold` entry referenced by the draft |

## Acceptance Criteria

- [ ] User can add a bookmark by providing a URL, a title, and an optional tag
- [ ] Each bookmark carries at most one tag (tag is optional, but is a single value, not a list)
- [ ] Adding a bookmark whose URL already exists is rejected with a clear error — duplicate URLs are not allowed (URL is the unique key)
- [ ] User can list all saved bookmarks
- [ ] User can find bookmarks by tag using **exact-match** comparison
- [ ] User can edit an existing bookmark's title and/or tag, identified by its URL
- [ ] User can delete a bookmark, identified by its URL
- [ ] Bookmarks persist to a local JSON file and are still present after the tool exits and is restarted
- [ ] The storage file path is configurable via a CLI flag **and** an environment variable, with a sensible default when neither is provided (precedence: CLI flag > env var > default)
- [ ] The tool is operable from the command line; a simple callable API is an acceptable alternative or companion interface

## Edge Cases

- Searching by a tag that matches zero bookmarks — must return an empty result gracefully, not crash
- Adding a bookmark whose URL already exists — must return a clear error and leave the store unchanged
- Editing or deleting a URL that is not present in the store — must return a clear error, not fail silently
- JSON file is absent on first run — tool must initialise a fresh, empty store rather than failing
- JSON file exists but is malformed or truncated (e.g. an interrupted write) — tool must surface a clear error rather than silently overwriting or corrupting data
- Tag or title containing special characters, quotes, or Unicode — must round-trip through JSON without corruption
- Adding a bookmark with no tag — tag is optional and must be accepted as absent/empty
- Both the CLI flag and the env var set for the storage path — CLI flag wins (documented precedence)

## Non-Functional Requirements

- Handles up to a few hundred bookmarks without noticeable latency
- Single-user only; no concurrent-access handling required ("used by one person at a time")
- Storage via a local JSON file only — no external database or network dependency
- Written in Kotlin/JVM per the team default for small internal tools; start from the existing `kotlin-scaffold` rather than a bare project

## Non-Goals

- Multi-user, shared, or remote bookmark storage
- Web UI, browser extension, or GUI of any kind
- Authentication, access control, or encryption of the bookmark file
- Cross-machine sync
- Import/export from third-party bookmark formats
- Multiple tags per bookmark

## Open Questions

_All resolved by the human during review (2026-07-04):_

1. **Editing** an existing bookmark's title/tag — ✅ in scope; edit title and tag, keyed by URL.
2. **Deleting** a bookmark — ✅ in scope; keyed by URL.
3. **Duplicate URLs** — ✅ rejected; URL is the unique key.
4. **Configurable storage path** — ✅ enable all: CLI flag and env var, with a sensible default (precedence CLI > env > default).
5. **Search semantics** — ✅ exact-match on tag.

## Changelog

- **2026-07-04** — Re-drafted from `sdlc/draft.md`. Acceptance criteria initially derived strictly from the draft (save / list / search-by-tag / local JSON persistence / CLI-or-API); editing, deletion, duplicate-URL handling, and configurable storage path were raised as Open Questions rather than assumed.
- **2026-07-04** — Human resolved all five open questions inline (edit ✅, delete ✅, dedup ✅, configurable path via CLI flag + env var ✅, exact-match tag search ✅). Folded the answers into Acceptance Criteria and Edge Cases; URL adopted as the unique key for add/edit/delete. Status set to **APPROVED** by direct human instruction to proceed to implementation.

---
*Do not edit `status` manually — set to `APPROVED` only by merging the reviewing PR.*
