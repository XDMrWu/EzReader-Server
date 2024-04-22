package com.wulinpeng.reader.route

import com.wulinpeng.reader.plugins.EzReaderRouteConfigure
import com.wulinpeng.reader.route.model.EzBook
import com.wulinpeng.reader.source.BookSourceManager
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.annotation.Single

@Single
class SearchBookRoute: EzReaderRouteConfigure {

    override fun config(route: Route) {
        with(route) {
            get ("/search") {
                val bookName = context.request.queryParameters["key"]
                if (bookName.isNullOrEmpty()) {
                    call.respondText(generateResponse(1, "Search key is empty"))
                    return@get
                }
                val result = BookSourceManager.search(bookName).map {
                    EzBook.fromBook(it)
                }
                call.respondText(generateResponse(result))
            }
        }
    }

}