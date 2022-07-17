package ui.element

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
@ExperimentalFoundationApi
fun TooltipIconButton(
    enabled: Boolean = true,
    onClick: () -> Unit,
    icon: ImageVector,
    tooltip: String,
    contentDescription: String = tooltip,
) {
    TooltipArea(
        tooltip = {
            Surface(
                modifier = Modifier
                    .shadow(4.dp)
                    .clickable(enabled = enabled, onClick = onClick),
                shape = RoundedCornerShape(4.dp),
            ) {
                Text(
                    text = tooltip,
                    modifier = Modifier.padding(10.dp),
                )
            }
        },
        content = {
            IconButton(onClick, enabled = enabled) { Icon(icon, contentDescription) }
        }
    )
}
