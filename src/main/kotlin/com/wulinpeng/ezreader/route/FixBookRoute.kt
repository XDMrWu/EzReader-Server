package com.wulinpeng.ezreader.route

import com.wulinpeng.ezreader.db.BookShelf
import com.wulinpeng.ezreader.plugins.EzReaderRouteConfigure
import com.wulinpeng.ezreader.route.model.EzBook
import com.wulinpeng.ezreader.source.BookSourceManager
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single

@Single
class FixBookRoute: EzReaderRouteConfigure {
    override fun config(route: Route) {
        with(route) {
            get("/fixBook") {
                val bookId = context.request.queryParameters["bookId"]
                if (bookId.isNullOrEmpty()) {
                    call.respondText(generateResponse(1, "BookId key is empty"))
                    return@get
                }
                // 查询书架信息
                val queryResult = transaction {
                    BookShelf.selectAll().where {BookShelf.bookId eq bookId}.singleOrNull()
                }
                if (queryResult == null) {
                    call.respondText(generateResponse(1, "BookShell not found bookId: $bookId"))
                    return@get
                }
                val bookName = queryResult[BookShelf.name]
                val author = queryResult[BookShelf.author]
                val (source, url) = parseId(bookId)
                val newBook = BookSourceManager.get().search(bookName, listOf(source)).firstOrNull { it.author == author }
                // 没有找到提到书源的书籍
                if (newBook == null) {
                    call.respondText(generateResponse(1, "No new book found"))
                    return@get
                }
                val newBookId = generateId(newBook.source, newBook.url)
                // 更新书架信息
                transaction {
                    BookShelf.update({BookShelf.bookId eq bookId}) {
                        it[BookShelf.bookId] = newBookId
                        it[BookShelf.image] = newBook.image
                        it[BookShelf.desc] = newBook.desc
                    }
                }
                call.respondText(generateResponse(EzBook.fromBook(newBook)))
            }
        }
    }
}