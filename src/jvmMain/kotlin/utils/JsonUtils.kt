package utils

import org.json.JSONArray
import org.json.JSONObject

fun <R> JSONArray.mapObjects(call: (obj: JSONObject) -> R): List<R> = map { call(it as JSONObject) }

@Suppress("UNCHECKED_CAST")
fun <R> JSONArray.findObject(call: (obj: R) -> Boolean): R? = find { call(it as R) } as? R?

fun JSONArray.toStringsList(): List<String> = map { it as String }

/**
 * Runs [JSONObject.has] and [JSONObject.isNull] to check that [this] contains an object at [key] and it's not null.
 * @author Arnau Mora
 * @since 20220717
 */
fun JSONObject.contains(key: String) = has(key) && !isNull(key)
