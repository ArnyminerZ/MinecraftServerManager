package mc

import data.JsonSerializable
import org.json.JSONArray
import org.json.JSONObject
import utils.findObject
import utils.httpGet

data class DownloadableJarFile(
    val url: String,
    val hash: String?,
    val size: Long?,
): JsonSerializable {
    companion object {
        fun fromJson(json: JSONObject): DownloadableJarFile =
            DownloadableJarFile(
                json.getString("url"),
                json.takeIf { it.has("hash") }?.getString("hash"),
                json.takeIf { it.has("size") }?.getLong("size"),
            )

        fun fromVanillaManifest(url: String): DownloadableJarFile {
            val manifest = httpGet(url).joinToString("")
            val manifestJson = JSONObject(manifest)
            val downloadsJson = manifestJson.getJSONObject("downloads")
            val serverDownloadJson = downloadsJson.getJSONObject("server")

            return DownloadableJarFile(
                serverDownloadJson.getString("url"),
                serverDownloadJson.getString("sha1"),
                serverDownloadJson.getLong("size"),
            )
        }

        fun fromFabricManifest(url: String): DownloadableJarFile {
            // Get the latest version for the Fabric loader available
            val loaderVersionsManifest = httpGet(url).joinToString("")
            val lvmJson = JSONArray(loaderVersionsManifest)
                .findObject<JSONObject> { it.getJSONObject("loader").getBoolean("stable") }!!
            val loaderJson = lvmJson.getJSONObject("loader")
            val loaderVersion = loaderJson.getString("version")
            val gameVersion = url.split("/").last()

            return DownloadableJarFile(
                "https://meta.fabricmc.net/v2/versions/loader/$gameVersion/$loaderVersion/0.11.0/server/jar",
                null,
                null,
            )
        }
    }

    override fun toJson(): JSONObject = JSONObject().apply {
        put("url", url)
        put("hash", hash)
        put("size", size)
    }
}
