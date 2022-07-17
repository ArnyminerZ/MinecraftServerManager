package ui.window

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.ExtensionOff
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Summarize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Terminal
import data.server.Server
import data.server.ServerStatus
import lang.getString
import manager.RunManager
import ui.action.drag.PlatformDropTargetModifier
import ui.element.ColumnOfListItem
import ui.element.ItemData
import ui.element.TitledWindow
import ui.element.TooltipIconButton
import ui.screen.server.ServerMods
import ui.screen.server.ServerPlayers
import ui.screen.server.ServerSummary
import ui.screen.server.ServerTerminal
import ui.screen.server.ServerWorld
import ui.theme.AppTheme
import ui.theme.onSuccessContainer
import ui.theme.onWarningContainer
import ui.theme.successContainer
import ui.theme.warningContainer
import utils.doAsync

@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@Composable
@ExperimentalMaterialApi
fun ServerViewWindow(
    serverData: Server,
    onCloseRequest: () -> Unit,
) {
    var server by remember { mutableStateOf(serverData) }
    val runningServer = RunManager.runningServers[server.id]

    TitledWindow(
        onCloseRequest = onCloseRequest,
        title = getString("window-view-server"),
    ) {
        val density = LocalDensity.current.density
        val dropParent = remember(density) {
            PlatformDropTargetModifier(
                density = density,
                window = window,
            )
        }

        AppTheme {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                var pageIndex by remember { mutableStateOf(0) }

                Card(
                    modifier = Modifier
                        .width(300.dp)
                        .fillMaxHeight()
                        .padding(8.dp),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    ColumnOfListItem(
                        listOf(
                            ItemData(Icons.Rounded.Summarize, getString("cd-server-summary")),
                            ItemData(Icons.Rounded.Public, getString("cd-server-world")),
                            if (server.type.supportsMods)
                                ItemData(Icons.Rounded.Extension, getString("cd-server-mods"))
                            else
                                ItemData(
                                    Icons.Rounded.ExtensionOff,
                                    getString("cd-server-mods-unsupported"),
                                    enabled = false
                                ),
                            ItemData(
                                Icons.Rounded.People,
                                getString("cd-server-players"),
                            ),
                            ItemData(
                                FontAwesomeIcons.Solid.Terminal,
                                getString("cd-server-terminal"),
                                enabled = runningServer != null,
                            ),
                        ),
                        pageIndex,
                        { pageIndex = it },
                    ) {
                        val backgroundColor = when (runningServer?.status?.value) {
                            ServerStatus.RUNNING -> successContainer
                            null -> MaterialTheme.colorScheme.secondaryContainer
                            else -> warningContainer
                        }
                        val contentColor = when (runningServer?.status?.value) {
                            ServerStatus.RUNNING -> onSuccessContainer
                            null -> MaterialTheme.colorScheme.onSecondaryContainer
                            else -> onWarningContainer
                        }

                        Spacer(Modifier.weight(1f))
                        Card(
                            modifier = Modifier
                                .padding(8.dp),
                            backgroundColor = backgroundColor,
                            contentColor = contentColor,
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                val preparingWorld = runningServer?.worldLoadingState?.value

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        when (runningServer?.status?.value) {
                                            ServerStatus.BOOTING -> if (preparingWorld?.finishedLoading == false && preparingWorld.loadingProgress >= 0)
                                                getString("status-server-loading-world", preparingWorld.loadingProgress)
                                            else
                                                getString("status-server-starting")
                                            ServerStatus.RUNNING -> getString("status-server-running")
                                            ServerStatus.STOPPING -> getString("status-server-stopping")
                                            else -> getString("status-server-not-running")
                                        },
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 8.dp),
                                        color = contentColor,
                                    )
                                    TooltipIconButton(
                                        onClick = {
                                            if (runningServer?.isRunning == true)
                                                RunManager.stop(server)
                                            else
                                                doAsync { RunManager.startServer(server) }
                                        },
                                        icon = if (runningServer?.isRunning == true)
                                            Icons.Rounded.Stop
                                        else
                                            Icons.Rounded.PlayArrow,
                                        tooltip = if (runningServer?.isRunning == true)
                                            getString("cd-stop")
                                        else
                                            getString("cd-start"),
                                        enabled = runningServer == null || runningServer.isRunning,
                                    )
                                }

                                AnimatedVisibility(
                                    preparingWorld != null && preparingWorld.loadingProgress >= 0 && !preparingWorld.finishedLoading,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    LinearProgressIndicator(
                                        preparingWorld?.loadingProgress?.div(100f) ?: 0f,
                                        color = contentColor,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    when (pageIndex) {
                        0 -> ServerSummary(server) { server = it }
                        1 -> ServerWorld(server)
                        2 -> ServerMods(server, Modifier.then(dropParent))
                        3 -> ServerPlayers(server, runningServer)
                        4 -> if (runningServer != null)
                            ServerTerminal(server, runningServer)
                        else pageIndex = 0
                    }
                }
            }
        }
    }
}
