package ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lang.getString
import ui.element.CardWithIcon
import ui.theme.onWarningContainer
import ui.theme.warningContainer

@Composable
fun UnsavedChangesCard(visible: Boolean) {
    AnimatedVisibility(visible) {
        CardWithIcon(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            backgroundColor = warningContainer,
            contentColor = onWarningContainer,
            icon = Icons.Rounded.WarningAmber,
            contentDescription = getString("cd-warning-save")
        ) {
            Text(
                getString("warning-unsaved-changes-title"),
                style = MaterialTheme.typography.titleMedium,
                color = onWarningContainer,
            )
            Text(
                getString("warning-unsaved-changes-message"),
                style = MaterialTheme.typography.bodyMedium,
                color = onWarningContainer,
            )
        }
    }
}
