package mc

import org.json.JSONArray
import org.json.JSONObject
import utils.findObject
import utils.mapObjects

enum class ServerType(
    val displayNameKey: String,
    val versionsProviderUrl: String,
    val supportsMods: Boolean,
    val versionProcessor: (manifest: String) -> List<VersionData>,
    val latestVersionProcessor: (manifest: String) -> Map<VersionType, String>,
) {
    VANILLA(
        "server-type-vanilla",
        "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json",
        false,
        { mani ->
            val manifest = JSONObject(mani)
            val versions = arrayListOf<VersionData>()
            val versionsJson = manifest.getJSONArray("versions")
            for (i in 0 until versionsJson.length()) {
                val versionJson = versionsJson.getJSONObject(i)
                versions.add(
                    VersionData(
                        versionJson.getString("id"),
                        versionJson
                            .getString("type")
                            .let { type ->
                                VersionType
                                    .values()
                                    .find { it.key == type }
                            },
                        versionJson.getString("url"),
                    )
                )
            }
            versions
        },
        { mani ->
            val manifest = JSONObject(mani)
            val latestVersions = hashMapOf<VersionType, String>()

            val latestArray = manifest.getJSONObject("latest")
            for (key in latestArray.keys())
                VersionType
                    .values()
                    .find { it.key == key }
                    ?.let { latestVersions[it] = latestArray.getString(key) }

            latestVersions
        }
    ),
    FABRIC(
        "server-type-fabric",
        "https://meta.fabricmc.net/v2/versions/game",
        true,
        { mani ->
            JSONArray(mani)
                .mapObjects {
                    VersionData(
                        it.getString("version"),
                        if (it.getBoolean("stable")) VersionType.RELEASE else VersionType.SNAPSHOT,
                        "https://meta.fabricmc.net/v2/versions/loader/${it.getString("version")}",
                    )
                }
        },
        { mani ->
            val manifest = JSONArray(mani)
            val stable = manifest.findObject<JSONObject> { it.getBoolean("stable") }?.getString("version")
            val snapshot = manifest.findObject<JSONObject> { !it.getBoolean("stable") }?.getString("version")

            hashMapOf<VersionType, String>().apply {
                stable?.let { this[VersionType.RELEASE] = it }
                snapshot?.let { this[VersionType.SNAPSHOT] = it }
            }
        }
    )
}