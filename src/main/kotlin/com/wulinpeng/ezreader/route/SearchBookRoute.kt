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
class SearchBookRoute: EzReaderRouteConfigure {

    override fun config(route: Route) {
        with(route) {
            get ("/search") {
                val bookName = context.request.queryParameters["key"]
                if (bookName.isNullOrEmpty()) {
                    call.respondText(generateResponse(1, "Search key is empty"), ContentType.Application.Json)
                    return@get
                }
                val result = BookSourceManager.get().search(bookName).map {
                    EzBook.fromBook(it)
                }
                call.respondText(generateResponse(result), ContentType.Application.Json)
            }
        }
    }

}