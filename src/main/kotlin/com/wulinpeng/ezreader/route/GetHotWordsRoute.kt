package com.wulinpeng.ezreader.route

import com.wulinpeng.ezreader.plugins.EzReaderRouteConfigure
import com.wulinpeng.ezreader.source.BookSourceManager
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

@Single
class GetHotWordsRoute: EzReaderRouteConfigure {
    override fun config(route: Route) {
        with(route) {
            get ("/hotWords") {
                val words = BookSourceManager.get().hotWords()
                call.respondText(generateResponse(HotWordsResponse(words)))
            }
        }
    }
}

@Serializable
data class HotWordsResponse(val words: List<String>)