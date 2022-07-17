package manager

import data.ModInformation
import data.server.Server
import lang.getString
import org.json.JSONException
import org.json.JSONObject
import utils.attempt
import utils.check
import utils.contains
import utils.json
import utils.toStringsList
import utils.unzip
import utils.write
import java.io.File

object ModManager {
    fun extractFabricMod(jarFile: File, server: Server): ModInformation? {
        val tempDir = utils.createTempDir("mcs-jar")
        val projectId = jarFile.name.let { it.substring(0, it.indexOf('-')) }
        val modDataDir = File(server.modsDirectory, projectId)

        modDataDir.mkdirs()
        jarFile.unzip(tempDir)

        val modInformation = File(tempDir, "fabric.mod.json")
            .takeIf { it.exists() }
            ?.copyTo(File(modDataDir, "fabric.mod.json"), true)
            ?.attempt<File, JSONException, ModInformation?>({ jsonFile ->
                jsonFile.json
                    .let { json ->
                        if (json.contains("icon"))
                            File(tempDir, json.getString("icon"))
                                .copyTo(File(modDataDir, "icon.png"), overwrite = true)
                        ModInformation(
                            jarFile,
                            json.getString("name"),
                            json.getString("description"),
                            json.getString("version"),
                            json.getJSONObject("contact")
                                .check(
                                    { it.contains("homepage") },
                                    { it.getString("homepage") }) { it.getString("sources") },
                            json.getJSONArray("authors").mapNotNull { author ->
                                if (author is String)
                                    author
                                else if (author is JSONObject && author.contains("name"))
                                    author.getString("name")
                                else null
                            },
                        )
                    }
                    .also { File(modDataDir, "info.json").write(it) }
            }, {
                ModInformation(
                    jarFile,
                    jarFile.name,
                    "",
                    getString("form-server-mods-unknown"),
                    "",
                    emptyList(),
                )
            })
        tempDir.deleteRecursively()
        return modInformation
    }
}