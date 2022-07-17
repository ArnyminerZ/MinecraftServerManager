package ui.screen.server

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import data.server.Server
import lang.getString
import manager.RunManager
import ui.component.RestartRequiredCard
import ui.component.UnsavedChangesCard

@Composable
fun ColumnScope.ServerSummary(server: Server, onServerUpdate: (newServer: Server) -> Unit) {
    var modified by remember { mutableStateOf(false) }
    var needsReload by remember { mutableStateOf(false) }
    var serverName by remember { mutableStateOf(server.name) }
    var networkProperties by remember { mutableStateOf(server.networkProperties) }

    fun checkForChanges() {
        modified = serverName != server.name || networkProperties != server.networkProperties
    }

    Column(
        modifier = Modifier
            .verticalScroll(state = rememberScrollState())
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .weight(1f),
    ) {
        OutlinedTextField(
            serverName,
            { serverName = it; checkForChanges() },
            maxLines = 1,
            singleLine = true,
            label = { Text(getString("form-server-name")) },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            OutlinedTextField(
                getString(server.type.displayNameKey),
                { },
                maxLines = 1,
                singleLine = true,
                readOnly = true,
                enabled = false,
                label = { Text(getString("form-server-type")) },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
            )
            OutlinedTextField(
                server.version.id,
                { },
                maxLines = 1,
                singleLine = true,
                readOnly = true,
                enabled = false,
                label = { Text(getString("form-server-version")) },
                modifier = Modifier.weight(1f),
            )
        }
        OutlinedTextField(
            networkProperties.port.toString(),
            { newPort ->
                newPort.toIntOrNull()
                    ?.let { networkProperties = networkProperties.copy(port = it) }
                checkForChanges()
            },
            maxLines = 1,
            singleLine = true,
            label = { Text(getString("form-server-port")) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
    }
    UnsavedChangesCard(modified)
    RestartRequiredCard(needsReload) { RunManager.reload(server); needsReload = false }
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.fillMaxWidth(),
    ) {
        AnimatedVisibility(modified) {
            OutlinedButton(
                modifier = Modifier.padding(horizontal = 4.dp),
                onClick = {
                    serverName = server.name
                    networkProperties = server.networkProperties
                },
            ) {
                Text(getString("action-undo"))
            }
        }

        Button(
            modifier = Modifier.padding(horizontal = 4.dp),
            enabled = modified,
            onClick = {
                // TODO: Save changes
                println("SERVER > Saving changes...")
                if (networkProperties != server.networkProperties) {
                    println("SERVER >   Network properties have been modified, saving...")
                    server.updateProperties(networkProperties)
                        .let(onServerUpdate)
                }
                if (server.isRunning)
                    needsReload = true
                checkForChanges()
            },
        ) {
            Text(getString("action-save"))
        }
    }
}