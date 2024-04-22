package com.wulinpeng.ezreader.source.impl

import com.wulinpeng.ezreader.plugins.defaultHttpClient
import com.wulinpeng.ezreader.source.core.Book
import com.wulinpeng.ezreader.source.core.BookSource
import com.wulinpeng.ezreader.source.core.Chapter
import com.wulinpeng.ezreader.source.core.CommonApi
import de.jensklingenberg.ktorfit.Ktorfit
import org.jsoup.Jsoup
import org.jsoup.nodes.TextNode
import org.koin.core.annotation.Single

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

    private fun parseBook(content: String): List<Book> {
        val document = Jsoup.parse(content)
        println(document)
        return document.getElementsByClass("novelslist2").first().getElementsByTag("ul").first().children().filter {
            it.tagName() == "li"
        }.mapIndexedNotNull { index, element ->
            if (index == 0) {
                return@mapIndexedNotNull null
            }
            val name = element.child(1).getElementsByTag("a").first().text()
            val author = element.child(2).getElementsByTag("a").first().text()
            val url = BASE_URL + element.child(1).getElementsByTag("a").first().attr("href").removePrefix("/")
            val lastUpdateTime = element.child(4).text()
            val lastUpdateChapter = element.child(3).getElementsByTag("a").first().text()
            Book(name, author, null, url, sourceName, null, lastUpdateTime = lastUpdateTime, lastUpdateChapter = lastUpdateChapter)
        }
    }

    override suspend fun getBookDetail(url: String): Book? {
        return runCatching {
            val response = api.getUrlContent(url)
            parseBookDetail(url, response)
        }.getOrNull()
    }

    private fun parseBookDetail(url: String, content: String): Book {
        val document = Jsoup.parse(content)
        val infoElement = document.getElementById("maininfo").getElementById("info")
        val name = infoElement.child(0).text()
        val author = infoElement.child(1).getElementsByTag("a").first().text()
        val image = BASE_URL + document.getElementById("fmimg").getElementsByTag("img").first().attr("src").removePrefix("/")
        val lastUpdateTime = infoElement.child(3).text().split("最后更新：").last()
        val desc = document.getElementById("maininfo").getElementById("intro").childNodes().filterIsInstance<TextNode>().map { it.text() }.joinToString("\n")
        val lastUpdateChapter = infoElement.children().last().text().removePrefix("最新章节：")
        val chapters = document.getElementById("list").child(0).children().let {
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
            document.getElementById("content").textNodes().map { it.text() }.joinToString("\n")
        }.getOrNull()
    }

    private fun parseChapterContent(content: String): String {
        val document = Jsoup.parse(content)
        return document.getElementById("ccc").childNodes().filterIsInstance<TextNode>().map { it.text() }.joinToString("\n")
    }
}