package com.wulinpeng.ezreader.route

import com.wulinpeng.ezreader.plugins.EzReaderRouteConfigure
import com.wulinpeng.ezreader.route.model.EzBook
import com.wulinpeng.ezreader.source.BookSourceManager
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.annotation.Single

@Single
class GetBookDetailRoute: EzReaderRouteConfigure {

    override fun config(route: Route) {
        with(route) {
            get ("/detail") {
                val bookId = context.request.queryParameters["bookId"]
                if (bookId.isNullOrEmpty()) {
                    call.respondText(generateResponse(1, "BookId key is empty"), ContentType.Application.Json)
                    return@get
                }
                val (source, url) = parseId(bookId)
                val book = BookSourceManager.get().detail(source, url)
                if (book == null) {
                    call.respondText(generateResponse(1, "Book not found"), ContentType.Application.Json)
                    return@get
                }
                call.respondText(generateResponse(EzBook.fromBook(book)), ContentType.Application.Json)
            }
        }
    }
}