package mc.api

import mc.Player
import org.json.JSONObject
import utils.SystemUtils
import utils.check
import utils.downloadFile
import utils.httpGet
import utils.image
import utils.mapObjects
import utils.write
import java.io.File
import java.util.Base64
import java.util.UUID

object MojangApiProvider {
    private const val accountsHost: String = "https://api.mojang.com"
    private const val sessionHost: String = "https://sessionserver.mojang.com"

    private val playersCacheDir = File(SystemUtils.appData, "player-cache")

    fun getUserUid(username: String) =
        JSONObject(httpGet("$accountsHost/users/profiles/minecraft/$username").joinToString(""))
            .getString("id")
            .let { UUID.fromString(it) }

    private fun getUserProperties(uid: UUID) =
        JSONObject(httpGet("$sessionHost/session/minecraft/profile/$uid").joinToString(""))
            .getJSONArray("properties")
            .mapObjects { it.getString("name") to it.getString("value") }
            .toMap()

    private fun getCachedPlayer(uuid: UUID) =
        playersCacheDir.takeIf { it.exists() }
            ?.let { File(it, "$uuid.json") }
            ?.takeIf { it.exists() && it.isFile }
            ?.let { JSONObject(it.readText()) }
            ?.let { Player.fromJson(it) }

    private fun storePlayerCache(player: Player) {
        // Create json file
        playersCacheDir
            // Create cache dir if doesn't exist
            .check({ !it.exists() }, { it.mkdirs(); it })
            // Initialize the target file
            .let { File(it, "${player.uuid}.json") }
            // Write the player's JSON
            .also { it.write(player) }

        // Download skin
        val playerCacheDir = File(playersCacheDir, player.uuid.toString())
            .check({ !it.exists() }, { it.mkdirs(); it })
        val skinHash = player.skinTextureUrl.substringAfterLast("/")
        val skinFile = File(playerCacheDir, "$skinHash.png")
        val headFile = File(playerCacheDir, "$skinHash-head.png")
        downloadFile(player.skinTextureUrl, skinFile, null)
        val skinImage = skinFile.image()
        val headImage = skinImage.getSubimage(8, 8, 8, 8)
        headFile.write(headImage)
    }

    fun getPlayerHead(player: Player) =
        player.skinTextureUrl
            .substringAfterLast("/")
            .let {
                val playerCacheDir = File(playersCacheDir, player.uuid.toString())
                File(playerCacheDir, "$it-head.png")
            }

    fun getPlayer(uuid: UUID): Player? =
        getCachedPlayer(uuid) ?: getUserProperties(uuid)
            // Get the textures property
            .takeIf { it.containsKey("textures") }
            // Decode the textures property
            ?.let { Base64.getDecoder().decode(it.getValue("textures")) }
            // Convert to String
            ?.let { String(it) }
            // This provides a JSON with player information
            ?.let { JSONObject(it) }
            // Initialize a new Player
            ?.let { playerInfo ->
                val textures = playerInfo.getJSONObject("textures")
                val skinUrl = textures.getJSONObject("SKIN").getString("url")
                val capeUrl = textures.takeIf { it.has("CAPE") }?.getJSONObject("CAPE")?.getString("url")
                Player(uuid, playerInfo.getString("profileName"), skinUrl, capeUrl)
            }
            ?.also { storePlayerCache(it) }
}