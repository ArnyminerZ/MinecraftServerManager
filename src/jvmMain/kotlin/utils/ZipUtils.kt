package utils

import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

private fun ZipEntry.newFile(targetDir: File): File = File(targetDir, name).also { targetFile ->
    val targetDirPath = targetDir.canonicalPath
    val targetFilePath = targetFile.canonicalPath

    if (!targetFilePath.startsWith(targetDirPath + File.separator))
        throw IOException("Entry is outside of the target dir: $name")
}

fun File.newUnzip(targetDir: File) {
    if (extension != "zip")
        return
    val buffer = ByteArray(1024)
    val file = ZipFile(this)
    file.entries()
        .asIterator()
        .forEach { entry ->
            val newFile = entry!!.newFile(targetDir)
            if (entry.isDirectory) {
                if (!newFile.isDirectory && !newFile.mkdirs())
                    throw IOException("Failed to create directory \"$newFile\"")
            } else {
                // fix for Windows-created archives
                val parent = newFile.parentFile
                if (!parent.isDirectory && !parent.mkdirs())
                    throw IOException("Failed to create directory \"$newFile\"")

                // Write file contents
                newFile
                    .outputStream()
                    .use { fos ->
                        var len: Int
                        val fis = file.getInputStream(entry)
                        while (fis.read(buffer).also { len = it } >= 0)
                            fos.write(buffer, 0, len)
                        fis.close()
                    }
            }
        }
    file.close()
}

@Deprecated(message = "Method has problems with EXT descriptors, use newUnzip", replaceWith = ReplaceWith("newUnzip"))
fun File.unzip(targetDir: File) {
    val buffer = ByteArray(1024)
    val zis = ZipInputStream(inputStream())
    var entry: ZipEntry?

    do {
        try {
            zis.nextEntry.also { entry = it }
        } catch (e: ZipException) {
            println("ZIP_UTILS > ERR! Got invalid file")
            e.printStackTrace()
            zis.nextEntry.also { entry = it }
            continue
        }
        if (entry == null)
            continue

        val newFile = entry!!.newFile(targetDir)
        if (entry!!.isDirectory) {
            if (!newFile.isDirectory && !newFile.mkdirs())
                throw IOException("Failed to create directory \"$newFile\"")
        } else {
            // fix for Windows-created archives
            val parent = newFile.parentFile
            if (!parent.isDirectory && !parent.mkdirs())
                throw IOException("Failed to create directory \"$newFile\"")

            // Write file contents
            newFile
                .outputStream()
                .use { fos ->
                    var len: Int
                    while (zis.read(buffer).also { len = it } >= 0)
                        fos.write(buffer, 0, len)
                }
        }
    } while (entry != null)
    zis.closeEntry()
    zis.close()
}
