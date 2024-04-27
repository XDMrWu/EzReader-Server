package com.wulinpeng.ezreader.coze

import com.wulinpeng.ezreader.plugins.defaultHttpClient
import com.wulinpeng.ezreader.route.defaultJson
import com.wulinpeng.ezreader.sse.client.readSse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

var cozeBotToken = ""

object CozeApi {
    suspend fun sendMessage(query: String): Flow<String> {
        val header = mapOf("Authorization" to "Bearer $cozeBotToken")
        val body = CozeBody("7362124251610005512", "UserDefault", query, true)
        return defaultHttpClient
            .readSse("https://api.coze.com/open_api/v2/chat", { header }, { body })
            .map {
                println(it)
                val obj = defaultJson.decodeFromString<JsonObject>(it.data)
                if (obj.get("event")!!.jsonPrimitive.content == "message" && obj.get("message")!!.jsonObject.get("type")!!.jsonPrimitive.content == "answer") {
                    obj.get("message")!!.jsonObject.get("content")!!.jsonPrimitive.content
                } else {
                    ""
                }
            }
    }
}

@Serializable
private data class CozeBody(val bot_id: String, val user: String, val query: String, val stream: Boolean)