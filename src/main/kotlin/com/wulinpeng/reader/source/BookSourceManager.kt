package com.wulinpeng.reader.source

import com.wulinpeng.reader.plugins.defaultKoin
import com.wulinpeng.reader.source.core.Book
import com.wulinpeng.reader.source.core.BookSource
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * author：wulinpeng
 * date：2021/10/14 16:58
 * desc:
 */
object BookSourceManager: CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob()

    private val sources: List<BookSource> = defaultKoin.getAll<BookSource>().sortedByDescending { it.priority }

    suspend fun search(bookName: String): List<Book> {
        val books =  sources.map {  source ->
            async(Dispatchers.IO) {
                source.searchBook(bookName)
            }
        }.awaitAll().filter { it.isNotEmpty() }.flatten()
        return books.groupBy { "${it.name}-${it.author}" }.map {
            it.value.first()
        }
    }

    suspend fun detail(source: String, url: String): Book? {
        return findSource(source)?.getBookDetail(url)
    }

    suspend fun content(source: String, url: String): String? {
        return findSource(source)?.getContent(url)
    }

    private fun findSource(sourceName: String): BookSource? {
        return sources.firstOrNull { it.sourceName == sourceName }
    }
}