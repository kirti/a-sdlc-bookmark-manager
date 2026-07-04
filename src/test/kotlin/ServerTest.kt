import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.test.*

class ServerTest {

    // Runs the module against a fresh, empty temp-file-backed store.
    private fun withServer(block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
        val path = createTempFile(prefix = "server-test-", suffix = ".json")
        path.deleteIfExists() // start empty
        application { bookmarkModule(BookmarkStore(path)) }
        try {
            block()
        } finally {
            path.deleteIfExists()
        }
    }

    private fun HttpRequestBuilder.json(body: String) {
        contentType(ContentType.Application.Json)
        setBody(body)
    }

    @Test
    fun `health returns 200`() = withServer {
        val res = client.get("/health")
        assertEquals(HttpStatusCode.OK, res.status)
    }

    @Test
    fun `add returns 201`() = withServer {
        val res = client.post("/bookmarks") { json("""{"url":"https://a.com","title":"A","tag":"news"}""") }
        assertEquals(HttpStatusCode.Created, res.status)
    }

    @Test
    fun `duplicate add returns 409`() = withServer {
        client.post("/bookmarks") { json("""{"url":"https://a.com","title":"A"}""") }
        val res = client.post("/bookmarks") { json("""{"url":"https://a.com","title":"A again"}""") }
        assertEquals(HttpStatusCode.Conflict, res.status)
    }

    @Test
    fun `malformed body returns 400`() = withServer {
        val res = client.post("/bookmarks") { json("""{"title":"missing url"}""") }
        assertEquals(HttpStatusCode.BadRequest, res.status)
    }

    @Test
    fun `list returns 200 and includes added bookmark`() = withServer {
        client.post("/bookmarks") { json("""{"url":"https://a.com","title":"A"}""") }
        val res = client.get("/bookmarks")
        assertEquals(HttpStatusCode.OK, res.status)
        assertTrue(res.bodyAsText().contains("https://a.com"))
    }

    @Test
    fun `search by tag returns only exact matches`() = withServer {
        client.post("/bookmarks") { json("""{"url":"https://a.com","title":"A","tag":"dev"}""") }
        client.post("/bookmarks") { json("""{"url":"https://b.com","title":"B","tag":"news"}""") }
        val hit = client.get("/bookmarks?tag=dev")
        assertEquals(HttpStatusCode.OK, hit.status)
        assertTrue(hit.bodyAsText().contains("https://a.com"))
        assertFalse(hit.bodyAsText().contains("https://b.com"))
    }

    @Test
    fun `search with no matches returns 200 empty array`() = withServer {
        val res = client.get("/bookmarks?tag=nope")
        assertEquals(HttpStatusCode.OK, res.status)
        assertEquals("[]", res.bodyAsText().trim())
    }

    @Test
    fun `update returns 200`() = withServer {
        client.post("/bookmarks") { json("""{"url":"https://a.com","title":"Old","tag":"old"}""") }
        val res = client.put("/bookmarks") { json("""{"url":"https://a.com","title":"New"}""") }
        assertEquals(HttpStatusCode.OK, res.status)
        assertTrue(res.bodyAsText().contains("New"))
    }

    @Test
    fun `update missing url returns 404`() = withServer {
        val res = client.put("/bookmarks") { json("""{"url":"https://missing.com","title":"X"}""") }
        assertEquals(HttpStatusCode.NotFound, res.status)
    }

    @Test
    fun `delete returns 200`() = withServer {
        client.post("/bookmarks") { json("""{"url":"https://a.com","title":"A"}""") }
        val res = client.delete("/bookmarks?url=https://a.com")
        assertEquals(HttpStatusCode.OK, res.status)
    }

    @Test
    fun `delete missing url returns 404`() = withServer {
        val res = client.delete("/bookmarks?url=https://missing.com")
        assertEquals(HttpStatusCode.NotFound, res.status)
    }

    @Test
    fun `delete without url param returns 400`() = withServer {
        val res = client.delete("/bookmarks")
        assertEquals(HttpStatusCode.BadRequest, res.status)
    }

    @Test
    fun `malformed store file returns 500`() = testApplication {
        val path = createTempFile(prefix = "server-bad-", suffix = ".json")
        java.nio.file.Files.writeString(path, "{ not valid json ")
        application { bookmarkModule(BookmarkStore(path)) }
        try {
            val res = client.get("/bookmarks")
            assertEquals(HttpStatusCode.InternalServerError, res.status)
        } finally {
            path.deleteIfExists()
        }
    }
}
