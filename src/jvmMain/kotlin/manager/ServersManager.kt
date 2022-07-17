package manager

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import data.server.NetworkProperties
import data.server.Server
import mc.ServerType
import mc.VersionData
import utils.*
import java.io.File

object ServersManager {
    enum class CreationStep {
        PREPARATION,
        DOWNLOAD,
        SECURITY_CHECK,
        MANIFEST_CREATION,
    }

    enum class CreationError {
        DIR_CREATION,
        HASH_MATCH,
    }

    interface CreationProgressUpdater {
        fun progress(step: CreationStep)

        fun downloadProgress(progress: MinMax?)

        fun error(error: CreationError)
    }

    /**
     * The directory where all the servers are stored.
     * @author Arnau Mora
     * @since 20220713
     */
    private val serversDirectory: File
        get() = File(SystemUtils.appData, "servers")

    private val jarsDirectory: File
        get() = File(SystemUtils.appData, "servers-jar")

    val serversList = mutableStateListOf<Server>()

    /**
     * @throws SecurityException – If a security manager exists and its [SecurityManager.checkRead] method does not
     * permit verification of the existence of the named directory and all necessary parent directories; or if the
     * [SecurityManager.checkWrite] method does not permit the named directory and all necessary parent directories to
     * be created.
     */
    suspend fun create(
        server: Server,
        listener: CreationProgressUpdater,
    ) {
        val serverRemoteJarFile = server.remoteJarFile

        //<editor-fold desc="Preparation">
        // Notify preparation state
        uiContext { listener.progress(CreationStep.PREPARATION) }

        // Initialize target files
        val dir = serverDirectory(server)
        val jar = File(jarsDirectory, Server.generateJarFileName(server.type, server.version))
        val manifestFile = File(dir, ".manifest.json")

        // Create directories if they don't exist
        dir.mkdirs().takeIf { it } ?: return listener.error(CreationError.DIR_CREATION)
        jarsDirectory.takeIf { !it.exists() }?.mkdirs()

        // Accept EULA
        File(dir, "eula.txt").writeText("eula=true")
        //</editor-fold>


        //<editor-fold desc="Download files">
        // Notify downloading state
        uiContext { listener.progress(CreationStep.DOWNLOAD) }

        // If jar is not downloaded, do it now
        if (!jar.exists())
            downloadFile(serverRemoteJarFile.url, jar) { listener.downloadProgress(MinMax(it, -1)) }
        //</editor-fold>

        // Check if the hash matches
        //<editor-fold desc="Security check">
        // Notify security check state
        uiContext { listener.progress(CreationStep.SECURITY_CHECK) }
        // Notify download has been finished
        uiContext { listener.downloadProgress(null) }

        // Check if hashes match
        if (serverRemoteJarFile.hash != null) {
            val jarHash = jar.sha1.toHexString()
            if (jarHash != serverRemoteJarFile.hash) {
                println("Hashes do not match. Downloaded=$jarHash, against=${serverRemoteJarFile.hash}")
                return listener.error(CreationError.HASH_MATCH)
            }
        }
        //</editor-fold>

        //<editor-fold desc="Create the server's manifest">
        uiContext { listener.progress(CreationStep.MANIFEST_CREATION) }
        manifestFile.writeText(server.toJson().toString(4))
        //</editor-fold>

        // Call all listeners
        getServers()
    }

    suspend fun getServers(): List<Server> =
        serversDirectory
            .list()
            .map { File(serversDirectory, it) }
            .filter { it.isDirectory }
            .filter { File(it, ".manifest.json").exists() }
            .map { serverDir ->
                val manifestFile = File(serverDir, ".manifest.json")
                val propertiesFile = File(serverDir, "server.properties").takeIf { it.exists() }
                val properties = propertiesFile?.parseYaml()
                Server.fromJson(
                    manifestFile.json,
                    networkProperties = properties?.let { NetworkProperties(it) } ?: NetworkProperties(),
                )
            }
            .also { uiContext { serversList.clear(); serversList.addAll(it) } }

    suspend fun deleteServer(server: Server) =
        serverDirectory(server)
            .deleteRecursively()
            // Update all the listeners with the new servers list
            .also { getServers() }

    fun serverDirectory(server: Server) = File(serversDirectory, server.category + "-" + server.id)

    fun serverJar(type: ServerType, version: VersionData) =
        File(jarsDirectory, Server.generateJarFileName(type, version))
}
