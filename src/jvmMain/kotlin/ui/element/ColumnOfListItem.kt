package ui.element

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Summarize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import lang.getString

class ItemData(
    val icon: ImageVector,
    val text: String,
    val contentDescription: String = text,
    val enabled: Boolean = true,
)

@ExperimentalMaterialApi
@Composable
fun ColumnOfListItem(
    items: List<ItemData>,
    selectedItem: Int,
    onItemSelected: (index: Int) -> Unit,
    selectedBackgroundColor: Color = MaterialTheme.colorScheme.tertiary,
    selectedContentColor: Color = MaterialTheme.colorScheme.onTertiary,
    inactiveBackgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    inactiveContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    extra: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Column {
        items.forEachIndexed { index, itemData ->
            ListItem(
                modifier = Modifier
                    .clickable(enabled = itemData.enabled) { onItemSelected(index) }
                    .background(if (selectedItem == index) selectedBackgroundColor else inactiveBackgroundColor),
                icon = {
                    Icon(
                        itemData.icon,
                        itemData.contentDescription,
                        tint = if (selectedItem == index) selectedContentColor else inactiveContentColor,
                        modifier = Modifier.size(24.dp),
                    )
                },
                text = {
                    Text(
                        itemData.text,
                        color = if (selectedItem == index) MaterialTheme.colorScheme.onTertiary else Color.Unspecified,
                        fontStyle = if (itemData.enabled) FontStyle.Normal else FontStyle.Italic,
                    )
                },
            )
        }
        extra?.invoke(this)
    }
}
