package ui.screen.server

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import data.ModInformation
import data.server.Server
import lang.getString
import ui.action.drag.dropTarget
import ui.theme.success
import ui.theme.warning
import ui.window.ModrinthDialog
import utils.doAsync
import utils.uiContext
import java.io.File

@Composable
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
fun ServerMods(server: Server, modifier: Modifier) {
    var showingModrinthDialog by remember { mutableStateOf(false) }
    var modsList by remember { mutableStateOf(emptyList<ModInformation>()) }
    var loadingMods by remember { mutableStateOf(false) }
    var borderColor by remember { mutableStateOf(Color.Unspecified) }
    var hoveringWithValidFile by remember { mutableStateOf(false) }

    if (showingModrinthDialog)
        ModrinthDialog(server) { showingModrinthDialog = false; modsList = server.getModsList() }

    if (modsList.isEmpty())
    doAsync {
        uiContext { loadingMods = true }
        val newModsList = server.getModsList()
        uiContext { modsList = newModsList; loadingMods = false }
    }

    val borderColorSelectable = warning
    val borderColorInvalid = MaterialTheme.colorScheme.error
    val borderColorHovering = success

    Column(
        modifier
            .fillMaxSize()
            .padding(top = 4.dp, end = 4.dp)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(8.dp))
            .dropTarget(
                onDragStarted = { files, _ ->
                    hoveringWithValidFile = files.isNotEmpty() && files.all { it.extension == "jar" }
                    borderColor = if (hoveringWithValidFile) borderColorSelectable else borderColorInvalid
                    true
                },
                onDragEntered = {
                    borderColor = if (hoveringWithValidFile) borderColorHovering else borderColorInvalid
                },
                onDragExited = {
                    borderColor = if (hoveringWithValidFile) borderColorSelectable else borderColorInvalid
                },
                onDragEnded = { borderColor = Color.Unspecified },
                onDropped = { files, _ ->
                    borderColor = Color.Unspecified
                    files
                        .takeIf { list -> list.isNotEmpty() && list.all { it.extension == "jar" } }
                        ?.also { println("Adding mods: $files") }
                        // Show the progress bar
                        ?.also { loadingMods = true }
                        // Create the mods directory if it doesn't exist
                        ?.also { if (!server.modsDirectory.exists()) server.modsDirectory.mkdirs() }
                        ?.let { modFiles ->
                            doAsync {
                                // Copy all the mod files
                                println("Copying files...")
                                modFiles.forEach { it.copyTo(File(server.modsDirectory, it.name)) }
                                // Refresh the mods list, which also extracts the data from the mods
                                println("Getting mods list...")
                                val newModsList = server.getModsList()
                                // Update the mods list
                                uiContext { modsList = newModsList; loadingMods = false }
                            }
                        }
                        ?.let { true } ?: false
                },
            )
    ) {
        Card(
            Modifier.fillMaxWidth()
                .padding(4.dp),
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    var showingModSourcesDropdown by remember { mutableStateOf(false) }
                    Column(
                        modifier = Modifier.weight(1f)
                            .padding(bottom = 4.dp),
                    ) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = getString("form-server-mods-list"),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = getString("form-drag-and-drop"),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                    IconButton(
                        onClick = { modsList = server.getModsList() },
                    ) {
                        Icon(Icons.Rounded.Refresh, getString("form-server-mods-reload"))
                    }
                    Box {
                        IconButton(
                            onClick = { showingModSourcesDropdown = true },
                        ) {
                            Icon(Icons.Rounded.Download, getString("form-server-mods-add"))
                        }
                        DropdownMenu(
                            showingModSourcesDropdown,
                            { showingModSourcesDropdown = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            mapOf<String, (() -> Unit)?>(
                                "form-server-mods-add-modrinth" to { showingModrinthDialog = true },
                            ).forEach { (stringResource, callback) ->
                                DropdownMenuItem(
                                    onClick = { callback?.invoke(); showingModSourcesDropdown = false },
                                    enabled = callback != null,
                                ) { Text(getString(stringResource)) }
                            }
                        }
                    }
                }
            }
        }
        AnimatedVisibility(loadingMods, Modifier.align(Alignment.CenterHorizontally)) {
            CircularProgressIndicator()
        }
        LazyColumn(Modifier.fillMaxSize().padding(4.dp)) {
            items(modsList) { info -> info.Card() }
        }
    }
}
