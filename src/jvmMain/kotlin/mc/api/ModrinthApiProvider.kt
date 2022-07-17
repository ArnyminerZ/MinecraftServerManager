package mc.api

import data.modrinth.ClientTypeSupport
import data.modrinth.ModLoader
import data.modrinth.ModrinthSearchResult
import data.modrinth.ModrinthVersion
import org.json.JSONArray
import org.json.JSONObject
import utils.httpGet
import utils.mapObjects
import java.net.URLEncoder

object ModrinthApiProvider {
    private const val base = "https://api.modrinth.com/v2"

    fun searchMod(query: String, version: String, loader: ModLoader): List<ModrinthSearchResult> {
        val facets = URLEncoder.encode(
            "[[\"project_type:mod\"],[\"versions:$version\"],[\"categories:${loader.name.lowercase()}\"]]",
            "utf-8",
        )
        val raw = httpGet("$base/search?query=$query&facets=$facets").joinToString("")
        return JSONObject(raw)
            .getJSONArray("hits")
            .mapObjects { ModrinthSearchResult.fromJson(it) }
            .filter { it.serverSide != ClientTypeSupport.UNSUPPORTED }
            .also { println("MODRINTH > Got ${it.size} results.") }
    }

    fun getModVersions(projectId: String, version: String, loader: ModLoader): List<ModrinthVersion> {
        val loaders = URLEncoder.encode("[\"${loader.name.lowercase()}\"]", "utf-8")
        val versions = URLEncoder.encode("[\"$version\"]", "utf-8")
        val raw = httpGet("$base/project/$projectId/version?loaders=$loaders&game_versions=$versions").joinToString("")
        return JSONArray(raw).mapObjects { ModrinthVersion.fromJson(it) }
    }
}
