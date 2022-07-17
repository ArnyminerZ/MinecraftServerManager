package data.server

data class Preferences(
    val minRamMb: Long = 512,
    val maxRamMb: Long = 1024,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Preferences

        if (minRamMb != other.minRamMb) return false
        if (maxRamMb != other.maxRamMb) return false

        return true
    }

    override fun hashCode(): Int {
        var result = minRamMb.hashCode()
        result = 31 * result + maxRamMb.hashCode()
        return result
    }
}
