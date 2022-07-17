package ui.dialog

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lang.getString

@Composable
@ExperimentalMaterialApi
@Suppress("FunctionName")
fun DeleteDialog(serverName: String, onDismissRequest: () -> Unit, onDeleteRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        buttons = {
            Row {
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.padding(horizontal = 4.dp),
                ) {
                    Text(getString("action-cancel"))
                }
                Button(
                    onClick = onDeleteRequest,
                    modifier = Modifier.padding(horizontal = 4.dp),
                ) {
                    Text(getString("action-delete"))
                }
            }
        },
        title = {
            Text(getString("dialog-delete-title"))
        },
        text = {
            Text(getString("dialog-delete-message", serverName))
        },
    )
}
