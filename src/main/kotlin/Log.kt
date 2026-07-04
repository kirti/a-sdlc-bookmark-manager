import java.time.Instant

/**
 * Minimal structured (logfmt) logger. One line per event, emitted to stdout, so Stage 9
 * monitoring can parse fields (component, op/method, result/status, latency_ms) without
 * scraping prose. Deliberately dependency-free — Ktor/Netty internals log via slf4j separately.
 */
object Log {
    fun event(vararg fields: Pair<String, Any?>) {
        val body = fields.joinToString(" ") { (k, v) -> "$k=${fmt(v)}" }
        println("ts=${Instant.now()} $body")
    }

    private fun fmt(v: Any?): String {
        val s = v?.toString() ?: "-"
        return if (s.isEmpty() || s.any { it.isWhitespace() || it == '=' }) "\"$s\"" else s
    }
}
