package data.server

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.JsonSerializable
import data.ModInformation
import data.modrinth.ModLoader
import lang.getString
import manager.ModManager
import manager.RunManager
import mc.DownloadableJarFile
import mc.ServerType
import mc.VersionData
import mc.WorldType
import org.json.JSONObject
import manager.ServersManager
import mc.api.MojangApiProvider
import mc.Player
import mc.api.ModrinthApiProvider
import org.json.JSONArray
import org.json.JSONException
import ui.element.TooltipIconButton
import utils.MinMax
import utils.add
import utils.check
import utils.doAsync
import utils.downloadFile
import utils.json
import utils.mapObjects
import utils.parseYaml
import utils.sha1
import utils.toHexString
import utils.unzip
import utils.writeYaml
import java.io.File
import java.io.FileNotFoundException
import java.util.UUID

data class Server(
    val name: String,
    val category: String,
    val version: VersionData,
    val type: ServerType,
    val remoteJarFile: DownloadableJarFile,
    val localJarFile: File,
    val worldProperties: WorldProperties = WorldProperties(),
    val networkProperties: NetworkProperties = NetworkProperties(),
    val preferences: Preferences = Preferences(),
) : JsonSerializable {
    companion object {
        fun fromJson(json: JSONObject, networkProperties: NetworkProperties = NetworkProperties()) =
            Server(
                json.getString("name"),
                json.getString("category"),
                VersionData.fromJson(json.getJSONObject("version")),
                ServerType.valueOf(json.getString("type")),
                DownloadableJarFile.fromJson(json.getJSONObject("remoteJarFile")),
                File(json.getString("localJarFile")),
                networkProperties = networkProperties,
            )

        fun generateJarFileName(type: ServerType, version: VersionData) =
            (type.toString() + version.toString()).sha1.toHexString() + ".jar"
    }

    val id: String = name.lowercase().replace(" ", "-")

    override fun toJson(): JSONObject = JSONObject().apply {
        put("name", name)
        put("category", category)
        put("version", version.toJson())
        put("type", type.name)
        put("remoteJarFile", remoteJarFile.toJson())
        put("localJarFile", localJarFile.path)
    }

    private val serverDirectory = ServersManager.serverDirectory(this)

    val worldDirectory: File
        get() = File(serverDirectory, worldProperties.worldName)

    val isWorldGenerated: Boolean
        get() = File(worldDirectory, "level.dat").exists()

    val isRunning: Boolean
        get() = RunManager.runningServers.containsKey(id)

    val serverJarFile: File = File(serverDirectory, "server.jar")

    val modsDirectory: File = File(serverDirectory, "mods")

    private val serverPropertiesFile: File = File(serverDirectory, "server.properties")
    private val opsFile: File = File(serverDirectory, "ops.json")

    fun updateProperties(networkProperties: NetworkProperties): Server {
        val properties = serverPropertiesFile
            .takeIf { it.exists() }
            ?.parseYaml()
            ?.toMutableMap()
            ?: throw FileNotFoundException("server.properties doesn't exist yet.")

        properties["server-port"] = networkProperties.port.toString()
        properties["enable-rcon"] = networkProperties.rconEnable.toString()
        properties["rcon.port"] = networkProperties.rconPort.toString()
        properties["rcon.password"] = networkProperties.rconPassword
        properties["enable-query"] = networkProperties.queryEnable.toString()
        properties["query.port"] = networkProperties.queryPort.toString()
        properties["enable-status"] = networkProperties.statusEnable.toString()
        properties["network-compression-threshold"] = networkProperties.networkCompressionThreshold.toString()
        properties["prevent-proxy-connections"] = networkProperties.preventProxyConnections.toString()
        properties["hide-online-players"] = networkProperties.hideOnlinePlayers.toString()

        serverPropertiesFile.writeYaml(properties)

        return copy(networkProperties = networkProperties)
    }

    fun isPlayerOp(player: Player): Boolean {
        if (!opsFile.exists())
            return false
        val opsJson = JSONArray(opsFile.readText())
        return opsJson.find { (it as JSONObject).getString("uuid") == player.uuid.toString() } != null
    }

    fun getCachedPlayers(): List<Player> =
        File(serverDirectory, "usercache.json")
            .takeIf { it.exists() }
            ?.let { JSONArray(it.readText()) }
            ?.mapObjects { UUID.fromString(it.getString("uuid")) }
            ?.mapNotNull { MojangApiProvider.getPlayer(it) }
            ?: emptyList()

    fun installMod(projectId: String, downloadProgress: (MinMax?) -> Unit) {
        // First check if already installed
        if (modsDirectory.takeIf { it.exists() }?.listFiles()?.find { it.name.startsWith("M_$projectId") } != null)
            return println("MOD_INSTALLER > Mod $projectId already installed.")

        println("MOD_INSTALLER > Installing $projectId...")
        val version = ModrinthApiProvider.getModVersions(
            projectId,
            version.id,
            ModLoader.valueOf(type.name),
        )[0]
        val file = version.files
            .sortedBy { it.primary }[0]
        val versionSize = file.size.toLong()
        val targetFile = File(modsDirectory, "M_$projectId-${file.filename}")
            .also { it.parentFile.takeIf { par -> !par.exists() }?.mkdirs() }

        // Download the file
        var bytes = 0L
        downloadProgress(MinMax(bytes, versionSize))
        downloadFile(file.url, targetFile) { bytesCount ->
            bytes += bytesCount
            downloadProgress(MinMax(bytes, versionSize))
        }
        downloadProgress(null)

        // Compare hashes
        if (!file.hashes.sha1.equals(targetFile.sha1.toHexString(), true))
            throw IllegalStateException("Hashes do not match.")

        // Decompress jar file to extract mod info
        ModManager.extractFabricMod(targetFile, this)

        // Resolve dependencies
        version.dependencies.forEach { dependency ->
            dependency.projectId?.let { installMod(it, downloadProgress) }
        }
    }

    fun getModsList(): List<ModInformation> =
        modsDirectory
            .takeIf { it.exists() }
            ?.listFiles()
            ?.filter { it.isFile && it.extension == "jar" }
            ?.mapNotNull { file ->
                val dataFolder = File(modsDirectory, file.name.let { it.substring(0, it.indexOf('-')) })
                File(dataFolder, "info.json")
                    .check({it.exists()}, { ModInformation.fromJson(it.json) }) {
                        if (type == ServerType.FABRIC)
                            try {
                                ModManager.extractFabricMod(file, this)
                            } catch (e: JSONException) {
                                println("Could not extract mod ($it). Error while parsing JSON")
                                e.printStackTrace()
                                null
                            } catch (e: ClassCastException) {
                                println("Could not extract mod ($it). Unexpected object.")
                                e.printStackTrace()
                                null
                            }
                        else null
                    }
            }
            ?: emptyList()

    @ExperimentalFoundationApi
    @Composable
    fun Card(modifier: Modifier = Modifier, onViewRequested:() -> Unit, onDeleteRequested: () -> Unit) {
        val runningServers = RunManager.runningServers
        val serverStatus = runningServers[id]?.status?.value

        androidx.compose.material.Card(
            modifier = modifier,
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            elevation = 5.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                Text(name)

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    TooltipIconButton(
                        onClick = {
                            if (serverStatus == ServerStatus.RUNNING)
                                RunManager.stop(this@Server)
                            else
                                doAsync { RunManager.startServer(this@Server) }
                        },
                        icon = if (serverStatus == ServerStatus.RUNNING)
                            Icons.Rounded.Stop
                        else
                            Icons.Rounded.PlayArrow,
                        tooltip = if (serverStatus == ServerStatus.RUNNING)
                            getString("cd-stop")
                        else
                            getString("cd-start"),
                        enabled = !runningServers.contains(id) || serverStatus == ServerStatus.RUNNING,
                    )
                    TooltipIconButton(
                        onClick = onViewRequested,
                        icon = Icons.Rounded.List,
                        tooltip = getString("cd-edit"),
                    )
                    TooltipIconButton(
                        onClick = onDeleteRequested,
                        icon = Icons.Rounded.Delete,
                        tooltip = getString("cd-delete"),
                    )
                }
            }
        }
    }
}

fun List<Server>.splitCategories(): Map<String, List<Server>> =
    hashMapOf<String, List<Server>>().apply {
        for (server in this@splitCategories)
            add(server.category, server)
    }
