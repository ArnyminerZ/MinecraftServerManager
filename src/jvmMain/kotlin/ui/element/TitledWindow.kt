package ui.element

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import ui.theme.AppTheme

@Composable
fun TitledWindow(
    onCloseRequest: () -> Unit,
    title: String,
    visible: Boolean = true,
    resizable: Boolean = true,
    state: WindowState = rememberWindowState(),
    content: @Composable FrameWindowScope.() -> Unit,
) {
    Window(
        onCloseRequest = onCloseRequest,
        title = title,
        visible = visible,
        resizable = resizable,
        state = state,
        icon = painterResource("icon.svg"),
    ) {
        AppTheme {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxSize(),
                ) { content() }
            }
        }
    }
}
