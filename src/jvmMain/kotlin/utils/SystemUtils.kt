package utils

import java.awt.Desktop
import java.io.File
import java.net.URI
import java.util.*

object SystemUtils {
    private val OS = System.getProperty("os.name").lowercase(Locale.getDefault())

    val isWindows: Boolean
        get() = OS.contains("win")

    val isMac: Boolean
        get() = OS.contains("mac")

    val isUnix: Boolean
        get() = OS.contains("nix") || OS.contains("nux") || OS.contains("aix")

    val homeDir: File
        get() = File(
            if (isWindows)
                System.getenv("LOCALAPPDATA")
            else
                System.getProperty("user.home")
        )

    val appData: File
        get() = File(homeDir, "MinecraftServerCreator")

    fun viewUrl(url: String) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
            Desktop.getDesktop().browse(URI(url))
        else {
            when {
                isWindows -> Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler $url")
                isMac -> Runtime.getRuntime().exec("open $url")
                isUnix -> {
                    val rt = Runtime.getRuntime()
                    val browsers = listOf("google-chrome", "firefox", "mozilla", "epiphany", "konqueror", "netscape", "opera", "links", "lynx")

                    val cmd = StringBuffer()
                    for (i in browsers.indices)
                        if (i == 0)
                            cmd.append("${browsers[i]} \"$url\"")
                        else
                            cmd.append(" || ${browsers[i]} \"$url\"")

                    rt.exec(arrayOf("sh", "-c", cmd.toString()))
                }
            }
        }
    }
}