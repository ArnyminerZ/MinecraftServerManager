package data.modrinth

enum class ProjectType {
    MOD, MODPACK;
    companion object {
        fun parse(text: String) = when(text.lowercase()) {
            "mod" -> MOD
            else -> MODPACK
        }
    }
}
