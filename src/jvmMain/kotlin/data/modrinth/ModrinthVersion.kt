package data.modrinth

import org.json.JSONObject
import utils.mapObjects
import utils.toStringsList
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ModrinthVersion(
    val name: String,
    val versionNumber: String,
    val changelog: String,
    val dependencies: List<ModrinthDependency>,
    val gameVersions: List<String>,
    val versionType: VersionType,
    val loaders: List<String>,
    val featured: Boolean,
    val id: String,
    val projectId: String,
    val authorId: String,
    val datePublished: LocalDateTime,
    val downloads: Int,
    val files: List<ModrinthFile>,
) {
    companion object {
        fun fromJson(json: JSONObject) = ModrinthVersion(
            json.getString("name"),
            json.getString("version_number"),
            json.getString("changelog"),
            json.getJSONArray("dependencies").mapObjects { ModrinthDependency.fromJson(it) },
            json.getJSONArray("game_versions").toStringsList(),
            VersionType.valueOf(json.getString("version_type").uppercase()),
            json.getJSONArray("loaders").toStringsList(),
            json.getBoolean("featured"),
            json.getString("id"),
            json.getString("project_id"),
            json.getString("author_id"),
            LocalDateTime.parse(json.getString("date_published"), DateTimeFormatter.ISO_DATE_TIME),
            json.getInt("downloads"),
            json.getJSONArray("files").mapObjects { ModrinthFile.fromJson(it) },
        )
    }
}