package com.wulinpeng.reader.route

import com.wulinpeng.reader.db.BookShelf
import com.wulinpeng.reader.plugins.EzReaderRouteConfigure
import com.wulinpeng.reader.source.BookSourceManager
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.statements.DeleteStatement.Companion.where
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single

@Single
class UpdateShelfRoute: EzReaderRouteConfigure {
    override fun config(route: Route) {
        with(route) {
            get("/updateShelf") {
                val bookId = context.request.queryParameters["bookId"]
                val type = context.request.queryParameters["type"]?.toInt() ?: 1
                val chapterNumber = context.request.queryParameters["chapter_number"]?.toInt() ?: 1
                if (bookId.isNullOrEmpty()) {
                    call.respondText(generateResponse(1, "Search key is empty"))
                    return@get
                }

                runCatching {
                    when(type) {
                        0 -> {
                            transaction {
                                BookShelf.deleteWhere { BookShelf.bookId eq bookId }
                            }
                            call.respondText(generateResponse(0, ""))
                        }
                        1 -> {
                            val (source, url) = parseId(bookId)
                            val book = BookSourceManager.detail(source, url)
                            if (book == null) {
                                call.respondText(generateResponse(1, "Book not found"))
                                return@get
                            }
                            transaction {
                                BookShelf.insert {
                                    it[BookShelf.bookId] = bookId
                                    it[BookShelf.progress] = chapterNumber
                                    it[BookShelf.name] = book.name
                                    it[BookShelf.author] = book.author
                                    it[BookShelf.image] = book.image
                                    it[BookShelf.desc] = book.desc
                                }
                            }
                            call.respondText(generateResponse(0, ""))
                        }
                        2 -> {
                            transaction {
                                BookShelf.update({
                                    BookShelf.bookId eq bookId
                                }) {
                                    it[BookShelf.progress] = chapterNumber
                                }
                            }
                            call.respondText(generateResponse(0, ""))
                        }
                    }
                }.onFailure {
                    call.respondText(generateResponse(1, it.message ?: "Unknown error"))
                }
            }
        }
    }

}