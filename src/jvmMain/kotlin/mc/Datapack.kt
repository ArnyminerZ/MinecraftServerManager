package mc

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import data.server.Server
import dev.dewy.nbt.tags.primitive.StringTag
import manager.DatapackManager
import java.io.File
import java.util.UUID

data class Datapack(
    val format: Int,
    val description: String?,
    val version: String?,
    val folder: File,
    val enabled: Boolean,
) {
    val image: File?
        get() = File(folder, "pack.png").takeIf { it.exists() }

    val id =
        description?.replace(" ", "-")
            ?.replace(Regex("§\\d"), "")
            ?.split('\n')
            ?.get(0)
            ?: UUID.randomUUID().toString()

    val coloredDescription: AnnotatedString
        get() = buildAnnotatedString {
            description?.let { desc ->
                println("Coloring description")
                var colorPosition = desc.indexOf('§')
                while (colorPosition >= 0) {
                    val colorChar = desc[colorPosition + 1]
                    val color = McColor.valueOf(colorChar)
                    val piece = desc.substring(
                        colorPosition + 2,
                        desc.indexOf('§', colorPosition + 1)
                            .takeIf { it >= 0 }
                            ?: desc.length,
                    )
                    if (color != null)
                        withStyle(SpanStyle(color = color.color)) {
                            append(piece)
                        }
                    else
                        append(piece)
                    colorPosition = desc.indexOf('§', colorPosition + 1)
                }
            }
        }

    private val relativePath = folder.relativeTo(folder.parentFile)

    val tag = StringTag("file/$relativePath")

    fun toggle(server: Server) = DatapackManager.toggle(server, this)
}
