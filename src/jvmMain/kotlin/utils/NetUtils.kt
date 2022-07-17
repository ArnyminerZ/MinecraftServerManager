package utils

import java.io.File
import java.io.IOException
import java.net.URL
import java.net.URLEncoder
import java.net.UnknownServiceException

const val BUFFER_SIZE = 1024

/**
 * Runs a http GET request to fetch the contents of the desired URL.
 * @author Arnau Mora
 * @since 20220713
 * @param url The URL to GET.
 * @throws IOException if an I/O exception occurs.
 * @throws UnknownServiceException if the protocol does not support input.
 */
@Throws(IOException::class, UnknownServiceException::class)
fun httpGet(url: String) =
    httpGet(URL(url))

/**
 * Runs a http GET request to fetch the contents of the desired URL.
 * @author Arnau Mora
 * @since 20220713
 * @param url The URL to GET.
 * @throws IOException if an I/O exception occurs.
 * @throws UnknownServiceException if the protocol does not support input.
 */
@Throws(IOException::class, UnknownServiceException::class)
private fun httpGet(url: URL): MutableList<String> =
    url
        .also { println("HTTP_GET > $url") }
        .openConnection()
        .getInputStream()
        .bufferedReader()
        .use { input ->
            input.lines().toList()
        }

/**
 * Downloads the file stored at the selected url into the target file.
 * @author Arnau Mora
 * @since 20220713
 * @param url The url to download.
 * @param target The [File] target to download the file to.
 * @param progressUpdate Will get called every [BUFFER_SIZE] bytes with the current download progress.
 * @throws IOException if an I/O exception occurs.
 * @throws UnknownServiceException if the protocol does not support input.
 */
@Throws(IOException::class, UnknownServiceException::class)
fun downloadFile(url: String, target: File, progressUpdate: ((bytes: Long) -> Unit)?) =
    downloadFile(URL(url), target, progressUpdate)

/**
 * Downloads the file stored at the selected url into the target file.
 * @author Arnau Mora
 * @since 20220713
 * @param url The url to download.
 * @param target The [File] target to download the file to.
 * @param progressUpdate Will get called every [BUFFER_SIZE] bytes with the current download progress.
 * @throws IOException if an I/O exception occurs.
 * @throws UnknownServiceException if the protocol does not support input.
 */
@Throws(IOException::class, UnknownServiceException::class)
fun downloadFile(url: URL, target: File, progressUpdate: ((bytes: Long) -> Unit)?) =
    url.openConnection()
        .getInputStream()
        .buffered(BUFFER_SIZE)
        .use { downloadStream ->
            progressUpdate?.let { pu ->
                target
                    .outputStream()
                    .use { fos ->
                        val buffer = ByteArray(BUFFER_SIZE)
                        var read: Int
                        var bytes = 0L
                        while (downloadStream.read(buffer, 0, BUFFER_SIZE).also { read = it } != -1) {
                            bytes += read.toLong()
                            pu(bytes)
                            fos.write(buffer, 0, read)
                        }
                    }
            } ?:
            // Simplified code for downloads without progress updates
            target
                .outputStream()
                .write(downloadStream.readBytes())
        }
