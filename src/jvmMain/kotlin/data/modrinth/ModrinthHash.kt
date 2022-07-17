package data.modrinth

import data.JsonSerializable
import org.json.JSONObject

data class ModrinthHash(
    val sha512: String,
    val sha1: String,
): JsonSerializable {
    companion object {
        fun fromJson(json: JSONObject) = ModrinthHash(
            json.getString("sha512"),
            json.getString("sha1"),
        )
    }

    override fun toJson(): JSONObject = JSONObject().apply {
        put("sha512", sha512)
        put("sha1", sha1)
    }
}
