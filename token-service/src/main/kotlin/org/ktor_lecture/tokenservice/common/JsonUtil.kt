package org.ktor_lecture.tokenservice.common

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


object JsonUtil {

    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    inline fun <reified T> encodeToJson(value: T): String {
        return json.encodeToString(value)
    }

    inline fun <reified T> decodeFromJson(jsonString: String): T {
        return json.decodeFromString(jsonString)
    }
}