package data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import data.server.Server
import data.server.ServerStatus
import exception.UnparseableLineException
import mc.LogLine
import mc.OnlinePlayer
import mc.Player

class RunningServer(
    private val server: Server,
) {
    val status = mutableStateOf(ServerStatus.STOPPED)
    val logs = mutableStateListOf<LogLine>()
    val worldLoadingState = mutableStateOf(WorldLoadingState.DEFAULT)
    val commandsBuffer = mutableStateListOf<String>()
    val players = mutableStateListOf<OnlinePlayer>()

    val isRunning: Boolean
        get() = status.value == ServerStatus.RUNNING

    fun clearCommandsBuffer() = commandsBuffer.clear()

    fun consumeCommands(call: (command: String) -> Unit) {
        commandsBuffer.forEach(call)
        clearCommandsBuffer()
    }

    fun notifyWorldLoadEnd() {
        worldLoadingState.value = worldLoadingState.value.copy(finishedLoading = true, loadingProgress = 100)
    }

    fun notifyWorldLoadProgress(progress: Int) {
        worldLoadingState.value = worldLoadingState.value.copy(finishedLoading = false, loadingProgress = progress)
    }

    fun notifyWorldLoadDimension(dimension: String) {
        worldLoadingState.value = worldLoadingState.value.copy(finishedLoading = false, dimension = dimension)
    }

    fun notifyPlayerJoined(player: Player) =
        players.add(OnlinePlayer(player, server.isPlayerOp(player)))

    fun notifyPlayerLeft(playerName: String) =
        players.removeIf { it.profileName == playerName }

    fun log(rawLine: String) = try {
        LogLine.parse(rawLine).also { logs.add(it) }
    } catch (_: UnparseableLineException) {
        null
    } catch (_: StringIndexOutOfBoundsException) {
        null
    }
}