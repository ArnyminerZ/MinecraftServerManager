package ui.window

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import data.modrinth.ModLoader
import data.modrinth.ModrinthSearchResult
import data.server.Server
import lang.getString
import mc.ServerType
import mc.api.ModrinthApiProvider
import ui.element.Card
import ui.theme.AppTheme
import utils.doAsync
import utils.uiContext

@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@Composable
fun ModrinthDialog(server: Server, onCloseRequest: () -> Unit) {
    var processOngoing by remember { mutableStateOf(false) }

    Dialog(
        onCloseRequest = { if (!processOngoing) onCloseRequest() },
        state = rememberDialogState(size = DpSize(600.dp, 500.dp)),
        title = "Modrinth",
        icon = painterResource("modrinth.svg"),
        resizable = false,
    ) {
        AppTheme {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
            ) { paddingValues ->
                var searchQuery by remember { mutableStateOf("") }
                var searching by remember { mutableStateOf(false) }
                var searchResults by remember { mutableStateOf(emptyList<ModrinthSearchResult>()) }

                val performSearch: () -> Unit = {
                    searching = true

                    doAsync {
                        val results = ModrinthApiProvider.searchMod(
                            searchQuery, server.version.id, when (server.type) {
                                ServerType.FABRIC -> ModLoader.FABRIC
                                ServerType.VANILLA -> ModLoader.FABRIC // This will never happen
                            }
                        )
                        uiContext {
                            searchResults = results
                            searching = false
                        }
                    }
                }

                Column(
                    Modifier.padding(paddingValues)
                ) {
                    OutlinedTextField(
                        searchQuery,
                        { searchQuery = it },
                        shape = RoundedCornerShape(100),
                        label = { Text(getString("form-mods-search")) },
                        modifier = Modifier
                            .fillMaxWidth(1f)
                            .padding(8.dp)
                            .onKeyEvent { event ->
                                when (event.key) {
                                    Key.Enter -> {
                                        performSearch()
                                        true
                                    }
                                    else -> false
                                }
                            },
                        singleLine = true,
                        maxLines = 1,
                        keyboardActions = KeyboardActions(onSend = { performSearch() }),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        trailingIcon = {
                            IconButton(
                                onClick = performSearch,
                            ) { Icon(Icons.Rounded.Search, getString("form-mods-search")) }
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary,
                        ),
                    )

                    AnimatedVisibility(
                        searching,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        CircularProgressIndicator()
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                    ) {
                        items(searchResults) { result ->
                            result.Card(server, { processOngoing = true }) { processOngoing = false }
                        }
                    }
                }
            }
        }
    }
}
