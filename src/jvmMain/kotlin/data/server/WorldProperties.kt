package data.server

import mc.WorldType

data class WorldProperties(
    // TODO: This should be fetched from properties file
    val worldName: String = "world",
    /**
     * @see <a href="https://minecraft.fandom.com/wiki/Seed_(level_generation)">Minecraft Wiki</a>
     */
    val worldSeed: String = "",
    /**
     * @see <a href="https://minecraft.fandom.com/wiki/World_type">Minecraft Wiki</a>
     */
    val worldType: WorldType = WorldType.NORMAL,
    val worldTypeCustom: String = "",
    val generateStructures: Boolean = true,
    val enableNether: Boolean = true,
)
