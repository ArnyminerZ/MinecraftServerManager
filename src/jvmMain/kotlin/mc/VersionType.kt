package mc

enum class VersionType(val key: String, val recommended: Boolean) {
    RELEASE("release", true),
    SNAPSHOT("snapshot", false)
}
