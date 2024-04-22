package com.wulinpeng.ezreader.route

import com.wulinpeng.ezreader.crypto.AESManager
import com.wulinpeng.ezreader.route.model.EmptyData
import com.wulinpeng.ezreader.route.model.EzResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val defaultJson = Json { ignoreUnknownKeys = true }

inline fun generateResponse(code: Int, msg: String): String {
    return generateResponse<EmptyData>(EzResponse(code, msg, null))
}

inline fun <reified T> generateResponse(data: T): String {
    return generateResponse<T>(EzResponse(0, "", data))
}

inline fun <reified T> generateResponse(code: Int, msg: String, data: T?): String {
    return generateResponse(EzResponse(code, msg, data))
}

inline fun <reified T> generateResponse(ezResponse: EzResponse<T>): String {
    return defaultJson.encodeToString(ezResponse)
}

fun generateId(source: String, url: String): String {
    return AESManager.encrypt("$source:$url")
}

fun parseId(id: String): Pair<String, String> {
    val split = AESManager.decrypt(id).split(":")
    return split[0] to split.subList(1, split.size).joinToString(":")
}