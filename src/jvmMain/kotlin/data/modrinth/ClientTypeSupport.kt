package data.modrinth

enum class ClientTypeSupport {
    REQUIRED, OPTIONAL, UNSUPPORTED;

    companion object {
        fun parse(text: String) = when(text.lowercase()) {
            "required" -> REQUIRED
            "optional" -> OPTIONAL
            else -> UNSUPPORTED
        }
    }
}
