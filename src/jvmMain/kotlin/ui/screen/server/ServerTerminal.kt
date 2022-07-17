package ui.screen.server

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import data.RunningServer
import data.server.Server
import kotlinx.coroutines.launch
import lang.getString
import manager.RunManager
import mc.LogLevel
import ui.theme.onSuccessContainer
import ui.theme.onWarningContainer
import ui.theme.successContainer
import ui.theme.warningContainer
import java.time.format.DateTimeFormatter

@ExperimentalComposeUiApi
@Composable
fun ColumnScope.ServerTerminal(server: Server, runningServer: RunningServer) {
        val scope = rememberCoroutineScope()
        val columnState = rememberLazyListState()

        val serverLogs = runningServer.logs

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            state = columnState,
        ) {
            items(serverLogs) { logLine ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    backgroundColor = when (logLine.level) {
                        LogLevel.INFO -> successContainer
                        LogLevel.WARN -> warningContainer
                        LogLevel.ERROR -> MaterialTheme.colorScheme.errorContainer
                    },
                    contentColor = when (logLine.level) {
                        LogLevel.INFO -> onSuccessContainer
                        LogLevel.WARN -> onWarningContainer
                        LogLevel.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                    },
                ) {
                    Column(Modifier.fillMaxWidth().padding(8.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                logLine.thread,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelLarge,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                logLine.time.toLocalTime().format(DateTimeFormatter.ISO_TIME),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                        Text(
                            logLine.line,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            scope.launch {
                try {
                    if (serverLogs.isNotEmpty())
                        columnState.scrollToItem(serverLogs.size - 1)
                } catch (_: IndexOutOfBoundsException) {
                }
            }
        }

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, end = 8.dp),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        var command by remember { mutableStateOf("") }
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            fun sendCommand() {
                val cmd = command
                    .replace("\n", "")
                    .replace("\r", "")
                    .takeIf { it.isNotBlank() }
                    ?: return
                RunManager.sendCommand(server, cmd)
                command = ""
            }

            TextField(
                command,
                { command = it },
                label = { Text(getString("placeholder-command")) },
                singleLine = true,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = { sendCommand() },
                ),
                modifier = Modifier
                    .weight(1f)
                    .onKeyEvent { keyEvent ->
                        when (keyEvent.key) {
                            Key.Enter -> {
                                sendCommand()
                                true
                            }
                            else -> false
                        }
                    },
            )
            IconButton(
                onClick = ::sendCommand,
            ) { Icon(Icons.Rounded.Send, getString("cd-send-command")) }
        }
    }
}
