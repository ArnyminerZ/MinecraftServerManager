package utils

class MinMax(val min: Long, val max: Long) {
    val progress: Double
        get() = min.toDouble() / max.toDouble()
}
