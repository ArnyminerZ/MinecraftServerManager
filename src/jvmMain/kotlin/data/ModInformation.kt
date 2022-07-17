package data

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import lang.getString
import org.json.JSONObject
import utils.SystemUtils
import utils.toComposeImageBitmap
import utils.toStringsList
import java.io.File

data class ModInformation(
    val jar: File,
    val name: String,
    val description: String,
    val version: String,
    val website: String?,
    val authors: List<String>,
) : JsonSerializable {
    companion object {
        fun fromJson(json: JSONObject): ModInformation = ModInformation(
            File(json.getString("jar")),
            json.getString("name"),
            json.getString("description"),
            json.getString("version"),
            json.getString("website"),
            json.getJSONArray("authors").toStringsList(),
        )
    }

    override fun toJson(): JSONObject = JSONObject().apply {
        put("jar", jar.path)
        put("name", name)
        put("description", description)
        put("version", version)
        put("website", website)
        put("authors", authors)
    }

    private val dataFolder = File(jar.parentFile, jar.name.let { it.substring(0, it.indexOf('-')) })

    val icon = File(dataFolder, "icon.png")

    @Composable
    fun Card() {
        androidx.compose.material.Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp, top = 4.dp, bottom = 4.dp),
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ) {
            Row(Modifier.fillMaxWidth()) {
                if (icon.exists())
                    Image(
                        icon.toComposeImageBitmap(),
                        name,
                        modifier = Modifier
                            .size(64.dp)
                            .padding(8.dp),
                    )
                else
                    Image(
                        painterResource("icon.svg"),
                        name,
                        modifier = Modifier
                            .size(64.dp)
                            .padding(8.dp),
                    )
                Column(Modifier.fillMaxWidth().padding(8.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Text(
                                authors.getOrNull(0) ?: "",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = ContentAlpha.medium),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    Text(
                        description,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                    Row(Modifier.fillMaxWidth()) {
                        website?.let { web ->
                            TextButton(
                                onClick = { SystemUtils.viewUrl(web) },
                            ) {
                                Icon(
                                    Icons.Rounded.Language,
                                    getString("form-server-mods-homepage"),
                                    modifier = Modifier.padding(end = 4.dp),
                                )
                                Text(getString("form-server-mods-homepage"))
                            }
                        }
                        TextButton(
                            onClick = { /* TODO: Delete mod */ },
                        ) {
                            Icon(
                                Icons.Rounded.Delete,
                                getString("form-server-mods-delete"),
                                modifier = Modifier.padding(end = 4.dp),
                            )
                            Text(getString("form-server-mods-delete"))
                        }
                    }
                }
            }
        }
    }
}
