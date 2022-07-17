package mc

import data.JsonSerializable
import lang.getString
import org.json.JSONObject

data class VersionData(
    val id: String,
    val type: VersionType?,
    val versionManifestUrl: String,
) : JsonSerializable {
    companion object {
        fun fromJson(json: JSONObject): VersionData =
            VersionData(
                json.getString("id"),
                VersionType.valueOf(json.getString("type")),
                json.getString("versionManifestUrl")
            )
    }

    override fun toJson(): JSONObject = JSONObject()
        .apply {
            put("id", id)
            put("type", type?.name)
            put("versionManifestUrl", versionManifestUrl)
        }
}

fun Iterable<VersionData>.versionTypes(): List<VersionType> {
    val versions = arrayListOf<VersionType>()
    for (ver in this)
        if (versions.size >= VersionType.values().size)
            break
        else if (!versions.contains(ver.type) && ver.type != null)
            versions.add(ver.type)
    return versions
}
