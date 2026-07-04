import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigTest {

    @Test
    fun `cli path wins over env and default`() {
        val path = resolveBookmarksPath("/cli/bookmarks.json", "/env/bookmarks.json")
        assertEquals("/cli/bookmarks.json", path.toString())
    }

    @Test
    fun `env path wins over default when no cli path`() {
        val path = resolveBookmarksPath(null, "/env/bookmarks.json")
        assertEquals("/env/bookmarks.json", path.toString())
    }

    @Test
    fun `falls back to default when neither cli nor env is set`() {
        val path = resolveBookmarksPath(null, null)
        assertEquals(defaultBookmarksPath(), path.toString())
    }
}
