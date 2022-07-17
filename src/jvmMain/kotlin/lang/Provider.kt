package lang

import utils.isNull

/**
 * Tries to get the String with key [key] in the currently loaded language.
 * @author Arnau Mora
 * @since 20220713
 * @param key The key of the string to fetch.
 * @param arguments Used for formatting the string following the [String.format] convention.
 * @throws IllegalStateException When no language has been loaded, or the loaded language doesn't have any strings in it.
 * @throws IllegalArgumentException When the [key] provided doesn't have a valid match.
 */
fun getString(key: String, vararg arguments: Any?): String =
    LangManager.localization
        .takeIf { it.isNotEmpty() }
        .isNull { throw IllegalStateException("No language has been loaded.") }
        ?.get(key)
        ?.format(*arguments)
        ?: throw IllegalArgumentException("Could not find string \"$key\"")
