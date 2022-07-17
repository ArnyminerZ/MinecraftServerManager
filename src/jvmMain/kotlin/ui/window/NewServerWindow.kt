package ui.window

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import data.server.Server
import lang.getString
import mc.DownloadableJarFile
import mc.ServerType
import mc.VersionData
import mc.VersionType
import mc.versionTypes
import manager.ServersManager
import ui.element.TitledWindow
import utils.MinMax
import utils.doAsync
import utils.firstCap
import utils.httpGet
import utils.toggle
import java.io.File

@Composable
@ExperimentalComposeUiApi
@Suppress("FunctionName")
fun NewServerWindow(
    visible: Boolean,
    onCloseRequest: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var availableServerTypes: Map<ServerType, List<VersionData>>? by remember { mutableStateOf(null) }
    var latestVersions by remember { mutableStateOf(emptyMap<ServerType, Map<VersionType, String>>()) }
    var availableVersions by remember { mutableStateOf(emptyList<VersionData>()) }

    doAsync {
        // Load all the available versions
        val availableVersionsMap = hashMapOf<ServerType, List<VersionData>>()
        val latestVersionsMap = hashMapOf<ServerType, Map<VersionType, String>>()
        println("Loading versions...")
        ServerType
            .values()
            .forEach { version ->
                println("Running GET to ${version.versionsProviderUrl}...")
                val manifest = httpGet(version.versionsProviderUrl).joinToString("")
                val versionsList = version.versionProcessor(manifest)
                val latestVersionsList = version.latestVersionProcessor(manifest)

                println("  Got ${versionsList.size} versions.")
                availableVersionsMap[version] = versionsList
                latestVersionsMap[version] = latestVersionsList
            }
        availableServerTypes = availableVersionsMap
        latestVersions = latestVersionsMap
    }

    TitledWindow(
        onCloseRequest = onCloseRequest,
        visible = visible,
        title = getString("window-new-server"),
        resizable = false,
        state = rememberWindowState(
            width = 500.dp,
            height = 550.dp,
        ),
    ) {
        if (availableServerTypes == null)
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                )
            }
        else
            Column(
                modifier = Modifier
                    .padding(8.dp),
            ) {
                var serverName: String by remember { mutableStateOf("") }
                var category: String by remember { mutableStateOf("") }
                var selectedType: ServerType? by remember { mutableStateOf(null) }
                var selectedVersion: VersionData? by remember { mutableStateOf(null) }

                var fieldsEnabled by remember { mutableStateOf(true) }

                var typeDropdownExpanded by remember { mutableStateOf(false) }
                var versionDropdownExpanded by remember { mutableStateOf(false) }

                var versionsFilter: Map<VersionType, Boolean> by remember { mutableStateOf(emptyMap()) }

                val categoryFocusRequester = remember { FocusRequester() }

                fun selectVersion(serverType: ServerType, versions: List<VersionData>) {
                    selectedType = serverType
                    typeDropdownExpanded = false
                    availableVersions = versions
                    versionsFilter = versions
                        .mapNotNull { version -> version.type?.let { it to it.recommended } }
                        .toMap()
                    val latestVersionMap = latestVersions[serverType]
                    selectedVersion = versions
                        .find { latestVersionMap?.get(VersionType.RELEASE) == it.id }
                }
                if (selectedType == null)
                    selectVersion(ServerType.VANILLA, availableServerTypes!!.getValue(ServerType.VANILLA))

                Text(
                    getString("form-new-server-title"),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(bottom = 8.dp),
                )
                OutlinedTextField(
                    serverName,
                    { serverName = it },
                    enabled = fieldsEnabled,
                    modifier = Modifier
                        .fillMaxWidth(),
                    label = {
                        Text(
                            getString("form-server-name")
                        )
                    },
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { categoryFocusRequester.requestFocus() }
                    ),
                )
                OutlinedTextField(
                    category,
                    { category = it },
                    enabled = fieldsEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(categoryFocusRequester),
                    label = {
                        Text(
                            getString("form-new-server-category")
                        )
                    },
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                        }
                    ),
                )
                Box(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        selectedType?.let { getString(it.displayNameKey) } ?: "",
                        onValueChange = {},
                        enabled = fieldsEnabled && availableServerTypes!!.isNotEmpty(),
                        readOnly = true,
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { state ->
                                if (state.isFocused) {
                                    typeDropdownExpanded = true
                                    println("Expanding types dropdown: $typeDropdownExpanded")
                                }
                            },
                        label = {
                            Text(
                                getString("form-server-type")
                            )
                        },
                    )
                    DropdownMenu(
                        typeDropdownExpanded,
                        onDismissRequest = {
                            typeDropdownExpanded = false
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        println("Displaying game types dropdown...")
                        ServerType
                            .values()
                            .also { println("There are ${it.size} server types available.") }
                            .forEach { serverType ->
                                DropdownMenuItem(
                                    onClick = {
                                        val versions = availableServerTypes!!.getValue(serverType)
                                        selectVersion(serverType, versions)
                                    },
                                ) {
                                    Text(
                                        getString(serverType.displayNameKey),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                    }
                }

                AnimatedVisibility(selectedVersion != null) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            availableVersions
                                .versionTypes()
                                .forEach { type ->
                                    OutlinedButton(
                                        enabled = fieldsEnabled,
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp),
                                        onClick = {
                                            versionsFilter = versionsFilter.toMutableMap().toggle(type)
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (versionsFilter[type] == true)
                                                MaterialTheme.colorScheme.primary
                                            else Color.Unspecified,
                                            contentColor = if (versionsFilter[type] == true)
                                                MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onBackground,
                                        )
                                    ) {
                                        Text(type.name.firstCap())
                                    }
                                }
                        }

                        Box(Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                selectedVersion?.id ?: "",
                                onValueChange = {},
                                enabled = fieldsEnabled && availableVersions.isNotEmpty(),
                                readOnly = true,
                                singleLine = true,
                                maxLines = 1,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { state ->
                                        if (state.isFocused)
                                            versionDropdownExpanded = true
                                    },
                                label = {
                                    Text(
                                        getString("form-server-version")
                                    )
                                },
                            )
                            DropdownMenu(
                                versionDropdownExpanded,
                                onDismissRequest = {
                                    versionDropdownExpanded = false
                                    focusManager.clearFocus()
                                },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant),
                            ) {
                                availableVersions
                                    .filter { versionsFilter[it.type] == true }
                                    .forEach { version ->
                                        DropdownMenuItem(
                                            onClick = {
                                                selectedVersion = version
                                                versionDropdownExpanded = false
                                            }
                                        ) {
                                            Text(
                                                version.id,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                            }
                        }
                    }
                }

                var serverCreationStatus: String? by remember { mutableStateOf(null) }
                var serverCreationError: String? by remember { mutableStateOf(null) }
                var serverCreationProgress: Float? by remember { mutableStateOf(0f) }

                Button(
                    enabled = fieldsEnabled && selectedVersion != null,
                    onClick = {
                        fieldsEnabled = false

                        doAsync {
                            val remoteJarFile = when (selectedType) {
                                ServerType.VANILLA -> DownloadableJarFile.fromVanillaManifest(selectedVersion!!.versionManifestUrl)
                                ServerType.FABRIC -> DownloadableJarFile.fromFabricManifest(selectedVersion!!.versionManifestUrl)
                                null -> throw NullPointerException("Selected type is null.")
                            }

                            ServersManager.create(
                                Server(
                                    serverName,
                                    category,
                                    selectedVersion!!,
                                    selectedType!!,
                                    remoteJarFile,
                                    ServersManager.serverJar(selectedType!!, selectedVersion!!),
                                ),
                                object : ServersManager.CreationProgressUpdater {
                                    override fun progress(step: ServersManager.CreationStep) {
                                        serverCreationStatus = getString(
                                            when (step) {
                                                ServersManager.CreationStep.PREPARATION -> "status-preparing"
                                                ServersManager.CreationStep.DOWNLOAD -> "status-downloading-server"
                                                ServersManager.CreationStep.SECURITY_CHECK -> "status-downloading-sha"
                                                ServersManager.CreationStep.MANIFEST_CREATION -> "status-manifesting"
                                            }
                                        )
                                    }

                                    override fun downloadProgress(progress: MinMax?) {
                                        serverCreationProgress = progress
                                            ?.takeIf { it.max >= 0 }
                                            ?.let { minMax ->
                                                val progressFloat = minMax.progress.toFloat()
                                                println("Server download progress: $progressFloat")
                                                progressFloat
                                            }
                                    }

                                    override fun error(error: ServersManager.CreationError) {
                                        serverCreationError = getString(
                                            when (error) {
                                                ServersManager.CreationError.DIR_CREATION -> "status-error-dir"
                                                ServersManager.CreationError.HASH_MATCH -> "status-error-hash"
                                            }
                                        )
                                    }
                                }
                            )

                            onCloseRequest()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Text(getString("action-create"))
                }

                AnimatedVisibility(!fieldsEnabled) {
                    val bgColor = if (serverCreationError != null)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                    val fgColor = if (serverCreationError != null)
                        MaterialTheme.colorScheme.onErrorContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        backgroundColor = bgColor,
                        contentColor = fgColor,
                    ) {
                        Column {
                            Text(
                                getString("status-creating-server"),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    serverCreationStatus ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                )
                                serverCreationProgress?.let {
                                    Text(
                                        "${(it * 100).toInt()} %",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                            serverCreationProgress?.let {
                                LinearProgressIndicator(
                                    it,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            } ?: LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
    }
}
