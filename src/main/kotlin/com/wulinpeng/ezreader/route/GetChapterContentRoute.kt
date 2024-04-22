package com.wulinpeng.ezreader.route

import com.wulinpeng.ezreader.plugins.EzReaderRouteConfigure
import com.wulinpeng.ezreader.source.BookSourceManager
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.annotation.Single

@Single
class GetChapterContentRoute: EzReaderRouteConfigure {

    override fun config(route: Route) {
        with(route) {
            get ("/content") {
                val chapterId = context.request.queryParameters["chapterId"]
                if (chapterId.isNullOrEmpty()) {
                    call.respondText(generateResponse(1, "ChapterId is empty"))
                    return@get
                }
                val (source, url) = parseId(chapterId)
                val content = BookSourceManager.content(source, url)
                if (content.isNullOrEmpty()) {
                    call.respondText(generateResponse(1, "Content not found"))
                    return@get
                }
                call.respondText(generateResponse(content))
            }
        }
    }
}