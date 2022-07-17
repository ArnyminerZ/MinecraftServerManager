import androidx.compose.animation.AnimatedVisibility
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.GridItemSpan
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.application
import data.server.Server
import data.server.ServerStatus
import data.server.splitCategories
import lang.LangManager
import lang.getString
import manager.PreferencesManager
import manager.RunManager
import manager.ServersManager
import manager.ServersManager.deleteServer
import ui.action.disableScrolling
import ui.dialog.DeleteDialog
import ui.element.TitledWindow
import ui.element.TooltipIconButton
import ui.screen.EulaScreen
import ui.window.NewServerWindow
import ui.window.ServerViewWindow
import utils.check
import utils.doAsync
import utils.uiContext
import java.util.Locale

@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalMaterial3Api::class,
)
@Preview
@Composable
@Suppress("FunctionName")
fun FrameWindowScope.App() {
    var showingCreateWindow by remember { mutableStateOf(false) }
    var viewingServer: Server? by remember { mutableStateOf(null) }

    val servers = ServersManager.serversList
    doAsync { ServersManager.getServers() }

    val eulaAccepted by remember { PreferencesManager.observeBoolean("eula-accepted") }

    if (!eulaAccepted)
        EulaScreen()
    else {
        var deleteServer: Server? by remember { mutableStateOf(null) }

        if (showingCreateWindow)
            NewServerWindow(
                showingCreateWindow,
                onCloseRequest = { showingCreateWindow = false },
            )

        viewingServer?.let { ServerViewWindow(it) { viewingServer = null } }

        if (deleteServer != null)
            DeleteDialog(
                deleteServer!!.name,
                onDismissRequest = { deleteServer = null },
                onDeleteRequest = {
                    doAsync {
                        deleteServer(deleteServer!!)
                        uiContext { deleteServer = null }
                    }
                },
            )

        AnimatedVisibility(servers.isEmpty()) {
            Box(Modifier.fillMaxSize()) {
                Card(
                    modifier = Modifier.widthIn(max = 400.dp).align(Alignment.Center),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    elevation = 5.dp
                ) {
                    Column(Modifier.padding(8.dp)) {
                        Text(
                            getString("server-list-empty-title"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            getString("server-list-empty-message"),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.padding(8.dp),
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
            ) {
                Button(
                    onClick = { showingCreateWindow = true },
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        getString("cd-new-server"),
                    )
                    Text(
                        getString("action-new-server")
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                servers
                    .splitCategories()
                    .forEach { (category, servers) ->
                        // Category header
                        item {
                            Text(
                                category,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp),
                            )
                        }

                        // Servers
                        items(servers) { server ->
                            server.Card(
                                Modifier
                                    .fillMaxWidth(if (window.width.dp < 400.dp) .5f else .3f)
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                { viewingServer = server },
                                { deleteServer = server },
                            )
                        }
                    }
            }
        }
    }
}

fun main() = application {
    LangManager.initialize(Locale.ENGLISH)

    TitledWindow(
        onCloseRequest = {
            RunManager.dispatchCommand("\u0000")
            exitApplication()
        },
        title = getString("app-name"),
    ) { App() }
}
