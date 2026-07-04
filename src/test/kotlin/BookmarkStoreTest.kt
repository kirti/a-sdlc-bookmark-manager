import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.test.*

class BookmarkStoreTest {

    private fun tempStore(): Pair<BookmarkStore, java.nio.file.Path> {
        val path = createTempFile(prefix = "bookmarks-test-", suffix = ".json")
        path.deleteIfExists() // start with absent file so store initialises empty
        return Pair(BookmarkStore(path), path)
    }

    @Test
    fun `store initialises empty when file is absent`() {
        val (store, path) = tempStore()
        try {
            assertTrue(store.list().isEmpty())
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `add succeeds and bookmark is persisted`() {
        val (store, path) = tempStore()
        try {
            val result = store.add("https://example.com", "Example", "news")
            assertIs<Result.Ok>(result)
            assertEquals(1, store.list().size)
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `add duplicate URL returns Err`() {
        val (store, path) = tempStore()
        try {
            store.add("https://example.com", "Example", null)
            val result = store.add("https://example.com", "Duplicate", null)
            assertIs<Result.Err>(result)
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `list returns all entries`() {
        val (store, path) = tempStore()
        try {
            store.add("https://one.com", "One", null)
            store.add("https://two.com", "Two", null)
            store.add("https://three.com", "Three", null)
            assertEquals(3, store.list().size)
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `findByTag returns exact matches only`() {
        val (store, path) = tempStore()
        try {
            store.add("https://kotlin.org", "Kotlin", "dev")
            store.add("https://gradle.org", "Gradle", "dev")
            store.add("https://news.ycombinator.com", "HN", "news")
            val devBookmarks = store.findByTag("dev")
            assertEquals(2, devBookmarks.size)
            assertTrue(devBookmarks.all { it.tag == "dev" })
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `findByTag does not return case-insensitive matches`() {
        val (store, path) = tempStore()
        try {
            store.add("https://example.com", "Example", "Dev")
            val results = store.findByTag("dev")
            assertTrue(results.isEmpty())
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `update succeeds and persists new values`() {
        val (store, path) = tempStore()
        try {
            store.add("https://example.com", "Old Title", "old-tag")
            val result = store.update("https://example.com", "New Title", "new-tag")
            assertIs<Result.Ok>(result)
            val updated = store.list().first { it.url == "https://example.com" }
            assertEquals("New Title", updated.title)
            assertEquals("new-tag", updated.tag)
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `update non-existent URL returns Err`() {
        val (store, path) = tempStore()
        try {
            val result = store.update("https://nonexistent.com", "Title", null)
            assertIs<Result.Err>(result)
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `delete succeeds and removes bookmark`() {
        val (store, path) = tempStore()
        try {
            store.add("https://example.com", "Example", null)
            val result = store.delete("https://example.com")
            assertIs<Result.Ok>(result)
            assertTrue(store.list().isEmpty())
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `delete non-existent URL returns Err`() {
        val (store, path) = tempStore()
        try {
            val result = store.delete("https://nonexistent.com")
            assertIs<Result.Err>(result)
        } finally {
            path.deleteIfExists()
        }
    }

    @Test
    fun `malformed store file surfaces a clear error`() {
        val path = createTempFile(prefix = "bookmarks-bad-", suffix = ".json")
        try {
            java.nio.file.Files.writeString(path, "{ this is not valid json ")
            val store = BookmarkStore(path)
            val ex = assertFailsWith<IllegalStateException> { store.list() }
            assertTrue(ex.message!!.contains(path.toString()))
        } finally {
            path.deleteIfExists()
        }
    }
}
