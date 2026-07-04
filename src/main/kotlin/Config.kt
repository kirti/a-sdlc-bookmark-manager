import java.nio.file.Path

private val DEFAULT_BOOKMARKS_FILE: String =
    "${System.getProperty("user.home")}/.bookmarks.json"

/**
 * Resolves the bookmarks JSON file path. Precedence: CLI flag > BOOKMARKS_FILE env var > default.
 * Shared by the CLI (Main) and the HTTP server (Server) so both honour the same configuration.
 */
fun resolveBookmarksPath(cliPath: String? = null): Path =
    resolveBookmarksPath(cliPath, System.getenv("BOOKMARKS_FILE"))

/**
 * Pure precedence logic, with the env value injected explicitly so it can be unit-tested
 * without mutating the process environment.
 */
internal fun resolveBookmarksPath(cliPath: String?, envPath: String?): Path {
    val chosen = cliPath ?: envPath ?: DEFAULT_BOOKMARKS_FILE
    return Path.of(chosen)
}

/** Exposed so tests can assert the default without duplicating the derivation. */
internal fun defaultBookmarksPath(): String = DEFAULT_BOOKMARKS_FILE
