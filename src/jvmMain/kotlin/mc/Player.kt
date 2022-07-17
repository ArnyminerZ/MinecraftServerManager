package mc

import data.JsonSerializable
import org.json.JSONObject
import java.util.UUID

open class Player(
    val uuid: UUID,
    val profileName: String,
    val skinTextureUrl: String,
    val capeTextureUrl: String?,
): JsonSerializable {
    companion object {
        fun fromJson(json: JSONObject): Player =
            Player(
                UUID.fromString(json.getString("uuid")),
                json.getString("profileName"),
                json.getString("skinTextureUrl"),
                json.getString("capeTextureUrl"),
            )
    }

    override fun toJson(): JSONObject = JSONObject().apply {
        put("uuid", uuid)
        put("profileName", profileName)
        put("skinTextureUrl", skinTextureUrl)
        put("capeTextureUrl", capeTextureUrl)
    }
}
