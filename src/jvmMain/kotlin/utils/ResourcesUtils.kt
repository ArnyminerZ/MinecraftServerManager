package utils

/**
 * Reads the contents of a file in the resources' directory at [path].
 * @author Arnau Mora
 * @since 20220713
 * @param path The path in the resources' directory to fetch.
 * @return null if file doesn't exist, or the contents of the file otherwise.
 */
fun getResourceAsText(path: String): String? =
    object {}.javaClass.getResource(path)?.readText()
