import kotlin.system.exitProcess

private fun parseArgs(args: Array<String>): Map<String, String> {
    val map = mutableMapOf<String, String>()
    var i = 0
    while (i < args.size) {
        val arg = args[i]
        if (arg.startsWith("--") && i + 1 < args.size) {
            map[arg.removePrefix("--")] = args[i + 1]
            i += 2
        } else {
            i++
        }
    }
    return map
}

private fun printBookmarks(bookmarks: List<Bookmark>) {
    bookmarks.forEach { b ->
        val tagPart = b.tag?.let { " [tag: $it]" } ?: ""
        println("${b.title} <${b.url}>$tagPart")
    }
}

/** Prints an error to stderr and returns a non-zero exit code (does not exit the process). */
private fun fail(message: String): Int {
    System.err.println("Error: $message")
    return 1
}

/** Runs a single subcommand and returns the process exit code (0 = success). */
private fun runCommand(command: String, params: Map<String, String>, store: BookmarkStore): Int {
    when (command) {
        "add" -> {
            val url = params["url"] ?: return fail("--url is required for 'add'")
            val title = params["title"] ?: return fail("--title is required for 'add'")
            return when (val result = store.add(url, title, params["tag"])) {
                is Result.Ok -> { println(result.message); 0 }
                is Result.Err -> fail(result.error)
            }
        }

        "list" -> {
            val bookmarks = store.list()
            if (bookmarks.isEmpty()) println("No bookmarks found.") else printBookmarks(bookmarks)
            return 0
        }

        "search" -> {
            val tag = params["tag"] ?: return fail("--tag is required for 'search'")
            val bookmarks = store.findByTag(tag)
            if (bookmarks.isEmpty()) println("No bookmarks found with tag '$tag'.") else printBookmarks(bookmarks)
            return 0
        }

        "update" -> {
            val url = params["url"] ?: return fail("--url is required for 'update'")
            return when (val result = store.update(url, params["title"], params["tag"])) {
                is Result.Ok -> { println(result.message); 0 }
                is Result.Err -> fail(result.error)
            }
        }

        "delete" -> {
            val url = params["url"] ?: return fail("--url is required for 'delete'")
            return when (val result = store.delete(url)) {
                is Result.Ok -> { println(result.message); 0 }
                is Result.Err -> fail(result.error)
            }
        }

        else -> return fail("Unknown command '$command'. Valid commands: add, list, search, update, delete")
    }
}

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        System.err.println(
            "Error: Usage: bookmark <command> [options]\n" +
            "Commands: add, list, search, update, delete"
        )
        exitProcess(1)
    }

    val command = args[0]
    val params = parseArgs(args.drop(1).toTypedArray())
    // --file overrides BOOKMARKS_FILE env var, which overrides the default path.
    val store = BookmarkStore(resolveBookmarksPath(params["file"]))

    val exitCode = try {
        runCommand(command, params, store)
    } catch (e: IllegalStateException) {
        // Store file present but unparseable/truncated — surface cleanly, not as a stack trace.
        Log.event("component" to "store", "op" to command, "result" to "io_error")
        fail(e.message ?: "Failed to read bookmarks file")
    }

    Log.event(
        "component" to "cli",
        "subcommand" to command,
        "result" to if (exitCode == 0) "ok" else "err",
        "exit_code" to exitCode
    )
    exitProcess(exitCode)
}
