package data.modrinth

import data.JsonSerializable
import org.json.JSONObject
import utils.contains

data class ModrinthDependency(
    val versionId: String?,
    val projectId: String?,
    val dependencyType: DependencyType,
): JsonSerializable {
    companion object {
        fun fromJson(json: JSONObject) = ModrinthDependency(
            json.takeIf { it.contains("version_id") }?.getString("version_id"),
            json.takeIf { it.contains("project_id") }?.getString("project_id"),
            DependencyType.valueOf(json.getString("dependency_type").uppercase()),
        )
    }

    override fun toJson(): JSONObject = JSONObject().apply {
        put("version_id", versionId)
        put("project_id", projectId)
        put("dependency_type", dependencyType)
    }
}
