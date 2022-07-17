package mc

enum class WorldType(val stringResourceKey: String) {
    NORMAL("world-type-normal"),
    FLAT("world-type-flat"),
    LARGE_BIOMES("world-type-large-biomes"),
    AMPLIFIED("world-type-amplified"),
    SINGLE_BIOME_SURFACE("world-type-single-biome-surface"),
    CUSTOM("world-type-custom"),
}
