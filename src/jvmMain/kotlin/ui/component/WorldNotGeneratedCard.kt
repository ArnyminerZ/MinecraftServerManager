package ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.server.Server
import lang.getString
import ui.dialog.FilePickerDialog
import ui.element.CardWithIcon
import ui.theme.onSuccessContainer
import ui.theme.successContainer
import utils.check

@Composable
fun WorldNotGeneratedCard(server: Server, onWorldImported: () -> Unit) {
    var showingWorldPicker by remember { mutableStateOf(false) }

    if (showingWorldPicker)
        FilePickerDialog(
            onCloseRequest = { files ->
                files.takeIf { it.isNotEmpty() }
                    ?.get(0)
                    ?.parentFile
                    ?.let { newWorld ->
                        server.worldDirectory
                            .apply { check({ it.exists() }) { it.deleteRecursively(); it } }
                            .let { newWorld.copyRecursively(it, true) }
                            .check({ it }, { println("World copied!"); onWorldImported() }) { println("ERR! Could not copy world") }
                    }
                showingWorldPicker = false
            },
            title = "Pick world", // TODO: Localize
            allowedExtensions = listOf("level.dat"),
        )

    CardWithIcon(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 16.dp),
        icon = Icons.Rounded.WarningAmber,
        contentDescription = getString("cd-warning-world-not-generated"),
        backgroundColor = successContainer,
        contentColor = onSuccessContainer,
    ) {
        Text(
            getString("warning-world-not-generated-title"),
            style = MaterialTheme.typography.titleMedium,
            color = onSuccessContainer,
        )
        Text(
            getString("warning-world-not-generated-message"),
            style = MaterialTheme.typography.bodyMedium,
            color = onSuccessContainer,
        )
        OutlinedButton(
            onClick = {
                showingWorldPicker = true
            }
        ) { Text(getString("action-import")) }
    }
}