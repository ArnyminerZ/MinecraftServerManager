package ui.element

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixNormal
import androidx.compose.material.icons.rounded.Biotech
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.PestControl
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import data.modrinth.ModLoader
import data.modrinth.ModrinthSearchResult
import data.server.Server
import lang.getString
import ui.utils.loadNetworkImage
import utils.SystemUtils
import utils.doAsync
import utils.firstCap
import utils.uiContext

@Composable
fun ModrinthSearchResult.Card(server: Server, onInstallStarted: () -> Unit, onInstallFinished: () -> Unit) {
    val installed = isInstalled(server)
    var isInstalled by remember { mutableStateOf(installed) }
    var installButtonEnabled by remember { mutableStateOf(!installed) }
    var installProgress by remember { mutableStateOf<Float?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Row(Modifier.fillMaxWidth()) {
            iconUrl?.let {
                Image(
                    loadNetworkImage(it),
                    title,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(8.dp),
                )
            } ?: run {
                Image(
                    painterResource("modrinth.svg"),
                    title,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(8.dp),
                )
            }
            Column(Modifier.fillMaxWidth().padding(8.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            getString("form-mods-author", author),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = ContentAlpha.medium),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Icon(Icons.Rounded.Download, getString("form-mods-downloads"))
                            Text(
                                downloads.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 2.dp),
                            )
                            Text(
                                getString("form-mods-downloads"),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = ContentAlpha.medium),
                            )
                        }
                        Row(verticalAlignment = Alignment.Bottom) {
                            Icon(Icons.Rounded.FavoriteBorder, getString("form-mods-followers"))
                            Text(
                                follows.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 2.dp),
                            )
                            Text(
                                getString("form-mods-followers"),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = ContentAlpha.medium),
                            )
                        }
                    }
                }
                Text(
                    description,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    categories
                        .forEach { category ->
                            if (ModLoader.values().find { it.name.lowercase() == category } == null)
                                Row(
                                    Modifier.padding(start = 4.dp, end = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        when (category) {
                                            "adventure" -> Icons.Rounded.Explore
                                            "cursed" -> Icons.Rounded.PestControl
                                            "decoration" -> Icons.Rounded.Home
                                            "equipment" -> Icons.Rounded.Construction
                                            "food" -> Icons.Rounded.RestaurantMenu
                                            "library" -> Icons.Rounded.LibraryBooks
                                            "magic" -> Icons.Rounded.AutoFixNormal
                                            "misc" -> Icons.Rounded.Language
                                            "optimization" -> Icons.Rounded.Bolt
                                            "storage" -> Icons.Rounded.Inventory2
                                            "technology" -> Icons.Rounded.Biotech
                                            "utility" -> Icons.Rounded.Work
                                            "worldgen" -> Icons.Rounded.Public
                                            else -> Icons.Rounded.Close
                                        },
                                        category,
                                        modifier = Modifier.size(16.dp).padding(end = 4.dp),
                                    )
                                    Text(
                                        category.firstCap(),
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                }
                        }
                }
                AnimatedVisibility(installProgress != null) {
                    LinearProgressIndicator(
                        installProgress ?: 0f,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Row(Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = {
                            installButtonEnabled = false
                            onInstallStarted()
                            doAsync {
                                server.installMod(projectId) { minMax ->
                                    if (minMax == null) {
                                        isInstalled = true
                                        installProgress = null
                                    } else
                                        installProgress = minMax.progress.toFloat()
                                }
                                uiContext { onInstallFinished() }
                            }
                        },
                        enabled = installButtonEnabled && !isInstalled,
                    ) {
                        Icon(
                            if (isInstalled) Icons.Rounded.Check else Icons.Rounded.Download,
                            getString("cd-mod-install"),
                            modifier = Modifier.padding(end = 4.dp),
                        )
                        Text(getString(if (isInstalled) "cd-mod-installed" else "cd-mod-install"))
                    }
                    TextButton(
                        onClick = { SystemUtils.viewUrl("https://modrinth.com/mod/$slug") },
                    ) {
                        Icon(
                            Icons.Rounded.Language,
                            getString("cd-mod-view"),
                            modifier = Modifier.padding(end = 4.dp),
                        )
                        Text(getString("cd-mod-view"))
                    }
                }
            }
        }
    }
}
