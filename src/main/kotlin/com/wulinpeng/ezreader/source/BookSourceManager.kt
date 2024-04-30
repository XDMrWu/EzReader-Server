package com.wulinpeng.ezreader.source

import com.wulinpeng.ezreader.plugins.defaultKoin
import com.wulinpeng.ezreader.source.core.Book
import com.wulinpeng.ezreader.source.core.BookSource
import kotlinx.coroutines.*
import org.koin.core.annotation.Single
import kotlin.coroutines.CoroutineContext

/**
 * author：wulinpeng
 * date：2021/10/14 16:58
 * desc:
 */
@Single(binds = [CoroutineScope::class])
class BookSourceManager: CoroutineScope {

    companion object {
        fun get() = defaultKoin.get<BookSourceManager>()
    }

    override val coroutineContext: CoroutineContext = SupervisorJob()

    private val sources: List<BookSource> = defaultKoin.getAll<BookSource>().sortedByDescending { it.priority }

    suspend fun search(bookName: String, excludeSource: List<String> = emptyList()): List<Book> {
        val books =  sources.filter { it.sourceName !in excludeSource }.map {  source ->
            async(Dispatchers.IO) {
                source.searchBook(bookName)
            }
        }.awaitAll().filter { it.isNotEmpty() }.flatten()
        return books.groupBy { "${it.name}-${it.author}" }.map {
            it.value.first()
        }
    }

    suspend fun detail(source: String, url: String): Book? {
        return findSource(source)?.getBookDetail(url, false)
    }

    suspend fun content(source: String, url: String): String? {
        return findSource(source)?.getContent(url)
    }

    suspend fun hotWords(): List<String> {
        return sources.map {
            async(Dispatchers.IO) {
                it.getHotWords()
            }
        }.awaitAll().flatten().distinct()
    }

    private fun findSource(sourceName: String): BookSource? {
        return sources.firstOrNull { it.sourceName == sourceName }
    }
}