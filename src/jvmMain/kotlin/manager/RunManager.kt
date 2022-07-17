package manager

import androidx.compose.runtime.mutableStateMapOf
import data.RunningServer
import data.server.Server
import data.server.ServerStatus
import mc.Datapack
import mc.api.MojangApiProvider
import mc.Player
import java.util.UUID

object RunManager {
    val runningServers = mutableStateMapOf<String, RunningServer>()

    fun sendCommand(server: Server, command: String) =
        runningServers[server.id]?.commandsBuffer?.add(command)

    fun dispatchCommand(command: String) =
        runningServers.forEach { (_, runningServer) ->
            runningServer.commandsBuffer.add(command)
        }

    fun setDatapackStatus(server: Server, datapack: Datapack) =
        sendCommand(server, "datapack ${if (datapack.enabled) "enable" else "disable"} ${datapack.tag}")

    fun stop(server: Server) =
        sendCommand(server, "stop")

    fun reload(server: Server) =
        sendCommand(server, "reload")

    fun kick(server: Server, player: Player) =
        sendCommand(server, "kick ${player.profileName}")

    private fun setServerStatus(server: Server, status: ServerStatus) {
        runningServers[server.id]?.status?.value = status
    }

    private fun notifyWorldLoadEnd(server: Server) =
        runningServers[server.id]?.notifyWorldLoadEnd()

    private fun notifyWorldLoadProgress(server: Server, progress: Int) =
        runningServers[server.id]?.notifyWorldLoadProgress(progress)

    private fun notifyWorldLoadDimension(server: Server, dimension: String) =
        runningServers[server.id]?.notifyWorldLoadDimension(dimension)

    private fun notifyLogMessage(server: Server, rawMessage: String) =
        runningServers[server.id]?.log(rawMessage)

    fun startServer(server: Server) {
        runningServers[server.id] = RunningServer(server)
        setServerStatus(server, ServerStatus.BOOTING)

        println("SERVER > Will launch ${server.id}.")
        println("         Jar: ${server.localJarFile.path}")
        val maxRam = server.preferences.maxRamMb
        val minRam = server.preferences.minRamMb
        val pb = ProcessBuilder("java", "-Xmx${maxRam}M", "-Xms${minRam}M", "-jar", server.localJarFile.path, "nogui")
        pb.directory(server.serverJarFile.parentFile)
        val proc = pb.start()
        println("SERVER > Starting...")

        val output = proc.outputStream
        val input = proc.inputStream
        val err = proc.errorStream

        while (proc.isAlive) {
            // Check if there are any commands available, and send
            runningServers
                .getValue(server.id)
                .consumeCommands { command ->
                    if (command == "\u0000") {
                        proc.destroy()
                    } else {
                        println("CMD > $command")

                        if (command == "stop")
                            setServerStatus(server, ServerStatus.STOPPING)
                        output.write((command + "\n").toByteArray())
                        output.flush()
                    }
                }

            // Check if there's input available
            if (input.available() > 0) {
                val rin = ByteArray(input.available())
                input.read(rin, 0, rin.size)
                val line = String(rin) // .also { println("INFO > $it") }

                // Message interceptor
                /*if (line.contains("Environment:")) {
                    // Received environment information
                    val parameters = line.substring(line.lastIndexOf("Environment: ") + "Environment:".length)
                        .split(", ")
                        .map { it.split("=") }
                        .associate { it[0] to it[1].replace("'", "") }
                } else*/
                if (line.contains("Done", true)) {
                    setServerStatus(server, ServerStatus.RUNNING)
                    notifyWorldLoadEnd(server)
                } else if (line.contains("Preparing level", true))
                    notifyWorldLoadProgress(server, 0)
                else if (line.contains("Preparing start region for dimension", true))
                    notifyWorldLoadDimension(server, line.substring(line.lastIndexOf(' ') + 1))
                else if (line.contains("Preparing spawn area", true))
                    notifyWorldLoadProgress(
                        server,
                        line
                            .substring(line.lastIndexOf(' ') + 1, line.length - 2)
                            .replace("%", "")
                            .also { println("Loading progress: $it") }
                            .toInt(),
                    )

                notifyLogMessage(server, line)
                    ?.also { println("INFO > $it") }
                    ?.also { logLine ->
                        if (logLine.thread.startsWith("User Authenticator")) {
                            val playerUuid = logLine.line
                                .substring(logLine.line.lastIndexOf(' ') + 1)
                                .filter { it.isLetterOrDigit() || it == '-' }
                            MojangApiProvider
                                .getPlayer(UUID.fromString(playerUuid))
                                ?.let { runningServers[server.id]?.notifyPlayerJoined(it) }
                        } else if (logLine.line.contains("lost connection")) {
                            val playerName = logLine.line.let { it.substring(0, it.indexOf(' ')) }
                            runningServers[server.id]?.notifyPlayerLeft(playerName)
                        }
                    }
            }

            // Check if there's error available
            if (err.available() > 0) {
                val rer = ByteArray(err.available())
                err.read(rer, 0, rer.size)
                println("ERROR > ${String(rer)}")
            }
        }

        println("SERVER > Stopped ${server.id}.")
        runningServers.remove(server.id)
    }
}