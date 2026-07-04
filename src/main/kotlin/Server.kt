import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable

@Serializable
data class AddRequest(val url: String, val title: String, val tag: String? = null)

@Serializable
data class UpdateRequest(val url: String, val title: String? = null, val tag: String? = null)

@Serializable
data class HealthResponse(val status: String = "ok")

@Serializable
data class DeleteResponse(val url: String, val deleted: Boolean = true)

@Serializable
data class ErrorResponse(val error: String)

private fun Result.resultTag(): String = when (this) {
    is Result.Ok -> "ok"
    is Result.Err.DuplicateUrl -> "duplicate_url"
    is Result.Err.NotFound -> "not_found"
    is Result.Err.Validation -> "validation"
}

private suspend fun ApplicationCall.respondError(status: HttpStatusCode, message: String) {
    respond(status, ErrorResponse(message))
}

/**
 * Thin HTTP layer over BookmarkStore. Handlers delegate to the store 1:1 and hold no
 * business logic. Store errors map to fixed HTTP statuses (see agent.md architecture rules).
 * The store is injectable so tests can point it at a temp file.
 */
fun Application.bookmarkModule(store: BookmarkStore = BookmarkStore(resolveBookmarksPath())) {
    install(ContentNegotiation) { json() }

    install(StatusPages) {
        exception<BadRequestException> { call, cause ->
            call.respondError(HttpStatusCode.BadRequest, cause.message ?: "Malformed request")
        }
        exception<SerializationException> { call, cause ->
            call.respondError(HttpStatusCode.BadRequest, "Malformed request body: ${cause.message}")
        }
        exception<IllegalStateException> { call, cause ->
            // Thrown by the store when the JSON file is present but unparseable.
            Log.event("component" to "store", "result" to "io_error")
            call.respondError(HttpStatusCode.InternalServerError, cause.message ?: "Internal error")
        }
        exception<Throwable> { call, cause ->
            call.respondError(HttpStatusCode.InternalServerError, cause.message ?: "Internal error")
        }
    }

    // One structured log line per request, with latency.
    intercept(ApplicationCallPipeline.Monitoring) {
        val start = System.nanoTime()
        proceed()
        val latencyMs = (System.nanoTime() - start) / 1_000_000
        Log.event(
            "component" to "http",
            "method" to call.request.httpMethod.value,
            "path" to call.request.path(),
            "status" to (call.response.status()?.value ?: 0),
            "latency_ms" to latencyMs
        )
    }

    routing {
        get("/health") {
            call.respond(HttpStatusCode.OK, HealthResponse())
        }

        route("/bookmarks") {
            post {
                val req = call.receive<AddRequest>()
                val result = store.add(req.url, req.title, req.tag)
                Log.event("component" to "store", "op" to "add", "url" to req.url, "result" to result.resultTag())
                when (result) {
                    is Result.Ok -> call.respond(HttpStatusCode.Created, Bookmark(req.url, req.title, req.tag))
                    is Result.Err.DuplicateUrl -> call.respondError(HttpStatusCode.Conflict, result.error)
                    is Result.Err -> call.respondError(HttpStatusCode.BadRequest, result.error)
                }
            }

            get {
                val tag = call.request.queryParameters["tag"]
                val bookmarks = if (tag != null) store.findByTag(tag) else store.list()
                Log.event(
                    "component" to "store",
                    "op" to if (tag != null) "findByTag" else "list",
                    "result" to "ok"
                )
                call.respond(HttpStatusCode.OK, bookmarks)
            }

            put {
                val req = call.receive<UpdateRequest>()
                val result = store.update(req.url, req.title, req.tag)
                Log.event("component" to "store", "op" to "update", "url" to req.url, "result" to result.resultTag())
                when (result) {
                    is Result.Ok -> {
                        val updated = store.list().firstOrNull { it.url == req.url }
                        if (updated != null) call.respond(HttpStatusCode.OK, updated)
                        else call.respondError(HttpStatusCode.InternalServerError, "Updated but not found on reload")
                    }
                    is Result.Err.NotFound -> call.respondError(HttpStatusCode.NotFound, result.error)
                    is Result.Err.Validation -> call.respondError(HttpStatusCode.BadRequest, result.error)
                    is Result.Err.DuplicateUrl -> call.respondError(HttpStatusCode.BadRequest, result.error)
                }
            }

            delete {
                val url = call.request.queryParameters["url"]
                    ?: return@delete call.respondError(HttpStatusCode.BadRequest, "Query parameter 'url' is required")
                val result = store.delete(url)
                Log.event("component" to "store", "op" to "delete", "url" to url, "result" to result.resultTag())
                when (result) {
                    is Result.Ok -> call.respond(HttpStatusCode.OK, DeleteResponse(url = url))
                    is Result.Err.NotFound -> call.respondError(HttpStatusCode.NotFound, result.error)
                    is Result.Err -> call.respondError(HttpStatusCode.BadRequest, result.error)
                }
            }
        }
    }
}

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    // Bind to loopback by default so the unauthenticated API is not exposed on the LAN.
    // Override with BIND_HOST=0.0.0.0 for container networking (the container is the boundary).
    val host = System.getenv("BIND_HOST") ?: "127.0.0.1"
    Log.event("component" to "http", "event" to "startup", "host" to host, "port" to port)
    embeddedServer(Netty, host = host, port = port) {
        bookmarkModule()
    }.start(wait = true)
}
