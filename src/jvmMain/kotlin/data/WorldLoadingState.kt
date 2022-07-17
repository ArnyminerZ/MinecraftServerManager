package data

data class WorldLoadingState(
    val finishedLoading: Boolean,
    val dimension: String,
    val loadingProgress: Int = -1,
) {
    companion object {
        val DEFAULT = WorldLoadingState(false, String(), -1)
    }
}
