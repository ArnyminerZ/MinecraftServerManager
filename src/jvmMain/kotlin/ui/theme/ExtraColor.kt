package ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val extra_color_dark_success = Color(0xff00e1ab)
val extra_color_dark_onSuccess = Color(0xff003828)
val extra_color_dark_successContainer = Color(0xff00513c)
val extra_color_dark_onSuccessContainer = Color(0xff35ffc5)
val extra_color_dark_warning = Color(0xff8e4f00)
val extra_color_dark_onWarning = Color(0xffffffff)
val extra_color_dark_warningContainer = Color(0xffffdcc1)
val extra_color_dark_onWarningContainer = Color(0xff2e1600)

val extra_color_light_success = Color(0xff006c50)
val extra_color_light_onSuccess = Color(0xffffffff)
val extra_color_light_successContainer = Color(0xff35ffc5)
val extra_color_light_onSuccessContainer = Color(0xff002116)
val extra_color_light_warning = Color(0xffffb877)
val extra_color_light_onWarning = Color(0xff4b2700)
val extra_color_light_warningContainer = Color(0xff6c3a00)
val extra_color_light_onWarningContainer = Color(0xffffdcc1)

val success
    @Composable
    get() = if (isSystemInDarkTheme())
        extra_color_dark_success
    else
        extra_color_light_success
val onSuccess
    @Composable
    get() = if (isSystemInDarkTheme())
        extra_color_dark_onSuccess
    else
        extra_color_light_onSuccess
val successContainer
    @Composable
    get() = if (isSystemInDarkTheme())
        extra_color_dark_successContainer
    else
        extra_color_light_successContainer
val onSuccessContainer
    @Composable
    get() = if (isSystemInDarkTheme())
        extra_color_dark_onSuccessContainer
    else
        extra_color_light_onSuccessContainer

val warning
    @Composable
    get() = if (isSystemInDarkTheme())
        extra_color_dark_warning
    else
        extra_color_light_warning
val onWarning
    @Composable
    get() = if (isSystemInDarkTheme())
        extra_color_dark_onWarning
    else
        extra_color_light_onWarning
val warningContainer
    @Composable
    get() = if (isSystemInDarkTheme())
        extra_color_dark_warningContainer
    else
        extra_color_light_warningContainer
val onWarningContainer
    @Composable
    get() = if (isSystemInDarkTheme())
        extra_color_dark_onWarningContainer
    else
        extra_color_light_onWarningContainer
