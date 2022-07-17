package utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import data.JsonSerializable
import org.json.JSONObject
import java.awt.image.BufferedImage
import java.io.File
import java.security.MessageDigest
import javax.imageio.ImageIO

val File.sha1: ByteArray
    get() {
        val digest = MessageDigest.getInstance("SHA-1")
        inputStream()
            .buffered(8192)
            .use { fis ->
                val buffer = ByteArray(8192)
                var read: Int
                while (fis.read(buffer).also { read = it } != -1) {
                    if (read > 0)
                        digest.update(buffer, 0, read)
                }
            }
        return digest.digest()
    }

val File.json: JSONObject
    get() = JSONObject(readText())

fun File.toComposeImageBitmap(): ImageBitmap =
    org.jetbrains.skia.Image.makeFromEncoded(readBytes()).toComposeImageBitmap()

fun File.write(serializable: JsonSerializable, indentFactor: Int = 2) =
    writeText(serializable.toJson().toString(indentFactor))

fun File.image(): BufferedImage = ImageIO.read(this)

fun File.write(image: BufferedImage, format: String = "png") = ImageIO.write(image, format, this)

fun createTempDir(prefix: String): File = kotlin.io.path.createTempDirectory(prefix).toFile()
