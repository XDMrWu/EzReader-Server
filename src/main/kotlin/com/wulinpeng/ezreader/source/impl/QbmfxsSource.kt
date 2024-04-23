package com.wulinpeng.ezreader.source.impl

import com.wulinpeng.ezreader.plugins.defaultHttpClient
import com.wulinpeng.ezreader.plugins.defaultKoin
import com.wulinpeng.ezreader.source.core.*
import de.jensklingenberg.ktorfit.Ktorfit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.jsoup.Jsoup
import org.koin.core.annotation.Single

@Single
class QbmfxsSource: BookSource {

    private val BASE_URL = "https://m.qbmfxs.com/"
    private val api = Ktorfit.Builder().httpClient(defaultHttpClient).baseUrl("$BASE_URL").build().create<CommonApi>()

    override val sourceName: String = "全本免费小说"
    override val priority: Int = 0

    override suspend fun searchBook(bookName: String): List<Book> {
        return runCatching {
            val response = api.getUrlContent("https://m.qbmfxs.com/search?kw=$bookName")
            parseBook(response)
        }.getOrElse { emptyList() }
    }

    private fun parseBook(content: String): List<Book> {
        val document = Jsoup.parse(content)
        return document.clazz("show").tag("ul").children().filter {
            it.tagName() == "a"
        }.mapIndexedNotNull { index, element ->
            val bookElement = element.clazz("book")
            val name = bookElement.child(0).text()
            val author = element.clazz("author").text()
            val url = BASE_URL + element.attr("href").removePrefix("/")
            val image = element.clazz("image").tag("img").attr("data-original")
            val desc = bookElement.child(1).text()
            val category = bookElement.clazz("type").text()
            Book(name, author, image, url, sourceName, desc, category = category)
        }
    }

    override suspend fun getBookDetail(url: String): Book? {
        return runCatching {
            val response = api.getUrlContent(url)
            parseBookDetail(url, response)
        }.getOrNull()
    }

    private suspend fun parseBookDetail(url: String, content: String): Book {
        val document = Jsoup.parse(content)
        val infoElement = document.clazz("book-intro")
        val name = infoElement.clazz("line_1").text()
        val author = infoElement.clazz("author").text()
        val image = infoElement.clazz("cover").tag("img").attr("data-original")
        val desc = infoElement.clazz("desc").tag("p").text()
        val category = infoElement.clazz("type").tag("a").text()
        // 获取章节
        val catalogUrl = BASE_URL + document.clazz("catalog").clazz("title").tag("a").attr("href").removePrefix("/")
        val chapters = parseCatalog(catalogUrl)
        return Book(name, author, image, url, sourceName, desc, null, null, category, chapters)
    }

    private suspend fun parseCatalog(catalogUrl: String): List<Chapter> {
        var firstDocument = Jsoup.parse(api.getUrlContent(catalogUrl))
        val pageSize = firstDocument.clazz("pagelist").tags("option").size
        val result = mutableListOf<Chapter>()
        return (1..pageSize).map {
            defaultKoin.get<CoroutineScope>().async {
                var document = firstDocument
                if (it > 1) {
                    // 由于是分页的，所以除了第一页以外需要重新获取
                    val pageUrl = catalogUrl + "/$it"
                    document = Jsoup.parse(api.getUrlContent(pageUrl))
                }
                document.clazz("catalog").tag("ul").tags("li").map {
                    Chapter(it.tag("p").textNodes().first().text(), BASE_URL + it.tag("a").attr("href").removePrefix("/"), sourceName)
                }
            }
        }.awaitAll().flatten()
    }

    override suspend fun getContent(url: String): String? {
        val firstDocument = Jsoup.parse(api.getUrlContent(url))
        val pageSize = firstDocument.id("main").tag("h1").text().split("/").last().removeSuffix(")").toInt()
        return (1..pageSize).map {
            defaultKoin.get<CoroutineScope>().async {
                var document = firstDocument
                if (it > 1) {
                    val pageUrl = url + "/$it"
                    document = Jsoup.parse(api.getUrlContent(pageUrl))
                }
                document.clazz("content").tags("p").filter {
                    it.className() != "read_tip" && !it.text().startsWith("本章未完")
                }.map { it.text() }.joinToString("\n")
            }
        }.awaitAll().joinToString("\n")
    }
}