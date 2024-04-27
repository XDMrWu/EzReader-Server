package com.wulinpeng.ezreader.route

import com.wulinpeng.ezreader.coze.CozeApi
import com.wulinpeng.ezreader.plugins.EzReaderRouteConfigure
import com.wulinpeng.ezreader.sse.SseEvent
import com.wulinpeng.ezreader.sse.server.respondSse
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import org.koin.core.annotation.Single

@Single
class ChatBotRoute: EzReaderRouteConfigure {
    override fun config(route: Route) {
        with(route) {
            get("/chatBot") {
                val query = context.request.queryParameters["query"]
                val flow = CozeApi.sendMessage(query ?: "").filter { !it.isNullOrEmpty() }.map {
                    SseEvent(data = defaultJson.encodeToString(SseData(it)))
                }

                call.respondSse(flow)
            }
        }
    }
}

@Serializable
data class SseData(val content: String)