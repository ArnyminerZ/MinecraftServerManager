package ui.screen.server

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import data.RunningServer
import data.server.Server
import lang.getString
import manager.RunManager
import mc.api.MojangApiProvider
import ui.theme.onSuccess
import ui.theme.success
import utils.toComposeImageBitmap

@Composable
fun ColumnScope.ServerPlayers(server: Server, runningServer: RunningServer?) {
    val cachedPlayers = server.getCachedPlayers()
    val players = runningServer?.players

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(cachedPlayers) { player ->
            val headFile = MojangApiProvider.getPlayerHead(player)
                .takeIf { it.exists() } ?: return@items
            val isOnline = players?.find { it.uuid == player.uuid } != null
            val isOp = server.isPlayerOp(player)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Image(
                        headFile.toComposeImageBitmap(),
                        player.profileName,
                        modifier = Modifier.size(64.dp),
                        filterQuality = FilterQuality.None,
                    )
                    Column(Modifier.weight(1f).padding(start = 8.dp)) {
                        Row(Modifier.fillMaxWidth()) {
                            Text(
                                player.profileName,
                                fontStyle = if (isOnline)
                                    FontStyle.Normal
                                else FontStyle.Italic,
                                modifier = Modifier.weight(1f),
                            )
                            if (isOp)
                                Badge(
                                    contentColor = MaterialTheme.colorScheme.error,
                                    containerColor = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                ) {
                                    Text(
                                        getString("form-server-players-status-admin"),
                                        modifier = Modifier.padding(horizontal = 4.dp),
                                    )
                                }
                            Badge(
                                contentColor = if (isOnline) onSuccess else Color.Unspecified,
                                containerColor = if (isOnline) success else Color.Unspecified,
                            ) {
                                Text(
                                    getString(if (isOnline) "form-server-players-status-online" else "form-server-players-status-offline"),
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                )
                            }
                        }
                        Row(Modifier.fillMaxWidth()) {
                            TextButton(
                                onClick = { RunManager.kick(server, player) },
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .weight(1f),
                                enabled = isOnline,
                            ) { Text(getString("form-server-players-action-kick")) }
                        }
                    }
                }
            }
        }
    }
}
