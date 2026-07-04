import java.nio.file.Path
import kotlin.system.exitProcess

private val DEFAULT_BOOKMARKS_FILE: String =
    "${System.getProperty("user.home")}/.bookmarks.json"

private fun resolveBookmarksPath(): Path {
    val envPath = System.getenv("BOOKMARKS_FILE")
    return Path.of(envPath ?: DEFAULT_BOOKMARKS_FILE)
}

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

private fun exitWithError(message: String): Nothing {
    System.err.println("Error: $message")
    exitProcess(1)
}

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        exitWithError(
            "Usage: bookmark <command> [options]\n" +
            "Commands: add, list, search, update, delete"
        )
    }

    val command = args[0]
    val remaining = args.drop(1).toTypedArray()
    val params = parseArgs(remaining)
    val store = BookmarkStore(resolveBookmarksPath())

    when (command) {
        "add" -> {
            val url = params["url"] ?: exitWithError("--url is required for 'add'")
            val title = params["title"] ?: exitWithError("--title is required for 'add'")
            val tag = params["tag"]
            when (val result = store.add(url, title, tag)) {
                is Result.Ok -> println(result.message)
                is Result.Err -> exitWithError(result.error)
            }
        }

        "list" -> {
            val bookmarks = store.list()
            if (bookmarks.isEmpty()) {
                println("No bookmarks found.")
            } else {
                bookmarks.forEach { b ->
                    val tagPart = b.tag?.let { " [tag: $it]" } ?: ""
                    println("${b.title} <${b.url}>$tagPart")
                }
            }
        }

        "search" -> {
            val tag = params["tag"] ?: exitWithError("--tag is required for 'search'")
            val bookmarks = store.findByTag(tag)
            if (bookmarks.isEmpty()) {
                println("No bookmarks found with tag '$tag'.")
            } else {
                bookmarks.forEach { b ->
                    val tagPart = b.tag?.let { " [tag: $it]" } ?: ""
                    println("${b.title} <${b.url}>$tagPart")
                }
            }
        }

        "update" -> {
            val url = params["url"] ?: exitWithError("--url is required for 'update'")
            val newTitle = params["title"]
            val newTag = params["tag"]
            when (val result = store.update(url, newTitle, newTag)) {
                is Result.Ok -> println(result.message)
                is Result.Err -> exitWithError(result.error)
            }
        }

        "delete" -> {
            val url = params["url"] ?: exitWithError("--url is required for 'delete'")
            when (val result = store.delete(url)) {
                is Result.Ok -> println(result.message)
                is Result.Err -> exitWithError(result.error)
            }
        }

        else -> exitWithError(
            "Unknown command '$command'. Valid commands: add, list, search, update, delete"
        )
    }
}
