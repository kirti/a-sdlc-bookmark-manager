import kotlinx.serialization.Serializable

@Serializable
data class Bookmark(val url: String, val title: String, val tag: String? = null)
