package utils

import java.security.MessageDigest

fun String.firstCap(): String =
    lowercase().replaceFirstChar { it.uppercaseChar() }

private val HEX_ARRAY = "0123456789ABCDEF".toCharArray()

fun ByteArray.toHexString(): String {
    val hexChars = CharArray(size * 2)
    for (j in indices) {
        val v = get(j).toInt() and 0xFF
        hexChars[j * 2] = HEX_ARRAY[v ushr 4]
        hexChars[j * 2 + 1] = HEX_ARRAY[v and 0x0F]
    }
    return String(hexChars).lowercase()
}

val String.sha1: ByteArray
    get() {
        val digest = MessageDigest.getInstance("SHA-1")
        val bytes = this.toByteArray(Charsets.ISO_8859_1)
        digest.update(bytes)
        return digest.digest()
    }
