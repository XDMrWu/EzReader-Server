package com.wulinpeng.reader.route

import com.wulinpeng.reader.plugins.EzReaderRouteConfigure
import com.wulinpeng.reader.route.model.EzBook
import com.wulinpeng.reader.source.BookSourceManager
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
                    call.respondText(generateResponse(1, "BookId key is empty"))
                    return@get
                }
                val (source, url) = parseId(bookId)
                val book = BookSourceManager.detail(source, url)
                if (book == null) {
                    call.respondText(generateResponse(1, "Book not found"))
                    return@get
                }
                call.respondText(generateResponse(EzBook.fromBook(book)))
            }
        }
    }
}