import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

sealed class Result {
    data class Ok(val message: String) : Result()
    data class Err(val error: String) : Result()
}

class BookmarkStore(private val filePath: Path) {

    private val json = Json { prettyPrint = true }

    private fun load(): List<Bookmark> {
        if (!Files.exists(filePath)) {
            return emptyList()
        }
        val content = Files.readString(filePath)
        return try {
            json.decodeFromString<List<Bookmark>>(content)
        } catch (e: Exception) {
            throw IllegalStateException(
                "Failed to parse bookmarks file at '$filePath': ${e.message}"
            )
        }
    }

    private fun save(bookmarks: List<Bookmark>) {
        val tmpPath = filePath.resolveSibling(filePath.fileName.toString() + ".tmp")
        val parent = filePath.parent
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent)
        }
        Files.writeString(tmpPath, json.encodeToString(bookmarks))
        Files.move(tmpPath, filePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
    }

    fun add(url: String, title: String, tag: String?): Result {
        val bookmarks = load().toMutableList()
        if (bookmarks.any { it.url == url }) {
            return Result.Err("Bookmark with URL '$url' already exists")
        }
        bookmarks.add(Bookmark(url = url, title = title, tag = tag))
        save(bookmarks)
        return Result.Ok("Bookmark added: $url")
    }

    fun list(): List<Bookmark> = load()

    fun findByTag(tag: String): List<Bookmark> = load().filter { it.tag == tag }

    fun update(url: String, newTitle: String?, newTag: String?): Result {
        if (newTitle == null && newTag == null) {
            return Result.Err("At least one of --title or --tag must be provided for update")
        }
        val bookmarks = load().toMutableList()
        val index = bookmarks.indexOfFirst { it.url == url }
        if (index == -1) {
            return Result.Err("Bookmark with URL '$url' not found")
        }
        val existing = bookmarks[index]
        bookmarks[index] = existing.copy(
            title = newTitle ?: existing.title,
            tag = newTag ?: existing.tag
        )
        save(bookmarks)
        return Result.Ok("Bookmark updated: $url")
    }

    fun delete(url: String): Result {
        val bookmarks = load().toMutableList()
        val removed = bookmarks.removeIf { it.url == url }
        if (!removed) {
            return Result.Err("Bookmark with URL '$url' not found")
        }
        save(bookmarks)
        return Result.Ok("Bookmark deleted: $url")
    }
}
