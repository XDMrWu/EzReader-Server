package com.wulinpeng.ezreader.source.impl

import com.wulinpeng.ezreader.plugins.defaultHttpClient
import com.wulinpeng.ezreader.source.core.*
import de.jensklingenberg.ktorfit.Ktorfit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.jsoup.Jsoup
import org.jsoup.nodes.TextNode
import org.koin.core.annotation.Single
import org.koin.core.component.get

@Single
class BiqusaSource: BookSource {

    private val BASE_URL = "https://www.biqusa.com/"
    private val api = Ktorfit.Builder().httpClient(defaultHttpClient).baseUrl("$BASE_URL").build().create<CommonApi>()

    override val sourceName: String = "爱曲小说"
    override val priority: Int = Int.MAX_VALUE

    override suspend fun searchBook(bookName: String): List<Book> {
        return runCatching {
            val response = api.getUrlContent("https://www.biqusa.com/search.html?name=$bookName")
            parseBook(response)
        }.getOrElse { emptyList() }
    }

    private suspend fun parseBook(content: String): List<Book> {
        val document = Jsoup.parse(content)
        return document.clazz("novelslist2").tag("ul").children().filter {
            it.tagName() == "li"
        }.mapIndexed { index, element ->
            get<CoroutineScope>().async(Dispatchers.IO) {
                if (index == 0) {
                    return@async null
                }
                val url = BASE_URL + element.child(1).tag("a").attr("href").removePrefix("/")
                getBookDetail(url, true)
            }
        }.awaitAll().filterNotNull()
    }

    override suspend fun getBookDetail(url: String, skipChapter: Boolean): Book? {
        return runCatching {
            val response = api.getUrlContent(url)
            parseBookDetail(url, response, skipChapter)
        }.getOrNull()
    }

    private fun parseBookDetail(url: String, content: String, skipChapter: Boolean): Book {
        val document = Jsoup.parse(content)
        val infoElement = document.id("maininfo").id("info")
        val name = infoElement.child(0).text()
        val author = infoElement.child(1).tag("a").text()
        val image = BASE_URL + document.id("fmimg").tag("img").attr("src").removePrefix("/")
        val lastUpdateTime = infoElement.child(3).text().split("最后更新：").last()
        val desc = document.id("maininfo").id("intro").childNodes().filterIsInstance<TextNode>().map { it.text() }.joinToString("\n")
        val lastUpdateChapter = infoElement.children().last().text().removePrefix("最新章节：")
        val chapters = if (skipChapter) null else document.id("list").child(0).children().let {
            val dtIndex = it.indexOfLast { it.tagName() == "dt" }
            it.subList(dtIndex + 1, it.size).map {
                val title = it.child(0).text()
                val chapterUrl = BASE_URL + it.child(0).attr("href").removePrefix("/")
                Chapter(title, chapterUrl, sourceName)
            }
        }
        return Book(name, author, image, url, sourceName, desc, lastUpdateTime, lastUpdateChapter, null, chapters)
    }

    override suspend fun getContent(url: String): String? {
        return runCatching {
            val response = api.getUrlContent(url)
            val document = Jsoup.parse(response)
            document.id("content").textNodes().map { it.text() }.joinToString("\n")
        }.getOrNull()
    }
}