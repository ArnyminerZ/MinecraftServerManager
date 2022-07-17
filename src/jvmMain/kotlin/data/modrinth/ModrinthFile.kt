package data.modrinth

import data.JsonSerializable
import org.json.JSONObject

data class ModrinthFile(
    val hashes: ModrinthHash,
    val url: String,
    val filename: String,
    val primary: Boolean,
    val size: Int,
): JsonSerializable {
    companion object {
        fun fromJson(json: JSONObject) = ModrinthFile(
            ModrinthHash.fromJson(json.getJSONObject("hashes")),
            json.getString("url"),
            json.getString("filename"),
            json.getBoolean("primary"),
            json.getInt("size"),
        )
    }

    override fun toJson(): JSONObject = JSONObject().apply {
        put("hashes", hashes.toJson())
        put("url", url)
        put("filename", filename)
        put("primary", primary)
        put("size", size)
    }
}
