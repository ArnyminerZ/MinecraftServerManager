package ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lang.getString
import manager.PreferencesManager
import utils.SystemUtils

@Composable
fun EulaScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Card(
            elevation = 5.dp,
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth(.7f)
                .align(Alignment.Center),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
            ) {
                Text(
                    getString("dialog-eula-title"),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    getString("dialog-eula-message")
                )
                Row {
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = {
                            SystemUtils.viewUrl("https://www.minecraft.net/en-us/eula")
                        },
                        modifier = Modifier.padding(horizontal = 4.dp),
                    ) {
                        Text(getString("action-read"))
                    }
                    Button(
                        onClick = { PreferencesManager.set("eula-accepted", true) },
                        modifier = Modifier.padding(horizontal = 4.dp),
                    ) {
                        Text(getString("action-accept"))
                    }
                }
            }
        }
    }
}
