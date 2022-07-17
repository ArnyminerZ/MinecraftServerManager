package data.modrinth

import data.server.Server
import org.json.JSONObject
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ModrinthSearchResult(
    val slug: String,
    val title: String,
    val description: String,
    val categories: List<String>,
    val clientSide: ClientTypeSupport,
    val serverSide: ClientTypeSupport,
    val projectType: ProjectType,
    val downloads: Int,
    val iconUrl: String?,
    val projectId: String,
    val author: String,
    val versions: List<String>,
    val follows: Int,
    val dateCreated: LocalDateTime,
    val dateModified: LocalDateTime,
    val latestVersion: String,
    val license: String,
    val gallery: List<String>,
) {
    companion object {
        fun fromJson(json: JSONObject) = ModrinthSearchResult(
            json.getString("slug"),
            json.getString("title"),
            json.getString("description"),
            json.getJSONArray("categories").map { it as String },
            ClientTypeSupport.parse(json.getString("client_side")),
            ClientTypeSupport.parse(json.getString("server_side")),
            ProjectType.parse(json.getString("project_type")),
            json.getInt("downloads"),
            json.takeIf { it.has("icon_url") }?.getString("icon_url"),
            json.getString("project_id"),
            json.getString("author"),
            json.getJSONArray("versions").map { it as String },
            json.getInt("follows"),
            LocalDateTime.parse(json.getString("date_created"), DateTimeFormatter.ISO_DATE_TIME),
            LocalDateTime.parse(json.getString("date_modified"), DateTimeFormatter.ISO_DATE_TIME),
            json.getString("latest_version"),
            json.getString("license"),
            json.getJSONArray("gallery").map { it as String },
        )
    }

    fun isInstalled(server: Server) =
        server.modsDirectory.takeIf { it.exists() }?.listFiles()?.find { it.name.startsWith("M_$projectId") } != null
}
