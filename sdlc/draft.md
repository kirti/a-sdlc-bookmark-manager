# Business Request: Bookmark Manager

**Requested by:** Internal tools team
**Priority:** Low

## Description

We keep losing track of useful internal links (dashboards, runbooks, docs) shared in
chat. We want a small tool to save a bookmark (a URL, a title, and an optional tag)
and list/search them later. Nothing fancy — command-line or simple callable API is
fine, this is for our own team's use.

## Known constraints

- No database available for this — needs to persist locally to a file so bookmarks
  survive between runs.
- Small scale: a few hundred bookmarks at most, used by one person at a time.
- Should be able to find a bookmark again by tag, not just by exact title.

## Technical notes (from engineering)

- Team default for small internal tools like this is Kotlin/JVM — reuse that unless
  there's a strong reason not to.
- Local JSON file for storage is fine given the "no database" constraint above.
- A working Kotlin/Gradle starter scaffold already exists at `~/scaffolds/kotlin-scaffold`
  (matches the `kotlin-scaffold` entry in the skill's `references/scaffold_registry.md`).
  Start from that instead of a bare project — copy it into this workspace as the
  starting point.