# Requirements: bookmark-manager

> Status: **DRAFT**
> Source: /Users/kirtikaushal/ai-skils/draft.md
> Generated: 2026-07-04

## Summary

A small command-line tool for the internal tools team to save, list, search, update, and delete bookmarks (URL + title + optional single tag). Bookmarks are persisted to a user-configurable local JSON file so they survive between runs. Target scale is a few hundred entries, single-user. Full CRUD is in scope.

## Acceptance Criteria

- [ ] User can add a bookmark by providing a URL, a title, and an optional tag
- [ ] Adding a bookmark whose URL already exists in the store returns an error — no duplicates allowed
- [ ] Each bookmark carries at most one tag (tag is optional, but cannot be a list)
- [ ] User can list all saved bookmarks
- [ ] User can search/filter bookmarks by tag using exact-match comparison
- [ ] User can update an existing bookmark's title and/or tag by URL
- [ ] User can delete a bookmark by URL
- [ ] Bookmarks persist to a local JSON file and are still present after the tool exits and is restarted
- [ ] The JSON file path is user-configurable (e.g. via a CLI flag or environment variable); there is a sensible default
- [ ] The tool is operable from the command line without additional setup beyond the JVM runtime

## Edge Cases

- Adding a bookmark whose URL already exists — must return a clear error and leave the store unchanged
- Updating or deleting a URL that does not exist in the store — must return a clear error
- Searching with a tag that matches zero bookmarks — must return an empty result gracefully, not crash
- JSON file is absent on first run — tool must initialise a fresh, empty store rather than failing
- JSON file exists but is malformed or truncated (e.g. interrupted write) — tool must surface a clear error rather than silently overwriting or corrupting data
- Tag or title contains special characters, quotes, or Unicode — must round-trip through JSON without corruption
- Extremely long URL or title strings — no defined limit; must not cause runtime errors

## Non-Functional Requirements

- Handles up to a few hundred bookmarks without noticeable latency
- Single-user only; no concurrent access handling required
- Storage via local JSON file (no external database or network dependency)
- Written in Kotlin/JVM per team default; start from the existing `kotlin-scaffold` rather than a bare project

## Non-Goals

- Multi-user or shared/remote bookmark storage
- Web UI, browser extension, or GUI of any kind
- Authentication, access control, or encryption of the bookmark file
- Cross-machine sync
- Import/export from third-party bookmark formats
- Multiple tags per bookmark

## Open Questions

_None._

---
*Do not edit `status` manually — set to `APPROVED` only by merging the reviewing PR.*
