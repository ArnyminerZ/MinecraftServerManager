package data

import org.json.JSONObject

interface JsonSerializable {
    fun toJson(): JSONObject
}