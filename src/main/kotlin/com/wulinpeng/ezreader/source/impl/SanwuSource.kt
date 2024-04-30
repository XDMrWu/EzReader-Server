package com.wulinpeng.ezreader.source.impl

import com.wulinpeng.ezreader.plugins.defaultHttpClient
import com.wulinpeng.ezreader.plugins.defaultKoin
import com.wulinpeng.ezreader.source.core.*
import de.jensklingenberg.ktorfit.Ktorfit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.TextNode
import org.koin.core.annotation.Single
import org.koin.core.component.get

/**
 * author：wulinpeng
 * date：2021/7/18 22:11
 * desc:
 */

@Single
class SanwuSource: BookSource {

    private val BASE_URL = "https://www.35wx.la/"
    private val api = Ktorfit.Builder().httpClient(defaultHttpClient).baseUrl("$BASE_URL").build().create<CommonApi>()

    override val sourceName: String = "三五第一小说网"
    override val priority: Int = 0

    override suspend fun searchBook(bookName: String): List<Book> {
        return runCatching {
            val response = api.postUrlContent("https://www.35wx.la/modules/article/search.php", SearchBody(bookName))
            parseBook(response)
        }.getOrElse { emptyList() }
    }

    private suspend fun parseBook(content: String): List<Book> {
        val document = Jsoup.parse(content)
        println(document)
        return document.getElementsByAttributeValue("id", "nr").map {
            get<CoroutineScope>().async(Dispatchers.IO) {
                val url = it.child(0).tag("a").attr("href")
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
        val author = infoElement.child(1).text().removePrefix("作者：").split(" ").first()
        val image = BASE_URL + document.id("fmimg").tag("img").attr("src").removePrefix("/")
        val lastUpdateTime = infoElement.child(1).text().split("最后更新：").last()
        val desc = document.id("maininfo").id("intro").child(0).childNodes().filterIsInstance<TextNode>().map { it.text() }.joinToString("\n")
        val lastUpdataChapter = infoElement.children().last().text().removePrefix("最新章节：")
        val chapters = if (skipChapter) null else document.id("list").child(0).children().let {
            val dtIndex = it.indexOfLast { it.tagName() == "dt" }
            it.subList(dtIndex + 1, it.size).map {
                val title = it.child(0).text()
                val chapterUrl = BASE_URL + it.child(0).attr("href").removePrefix("/")
                Chapter(title, chapterUrl, sourceName)
            }
        }
        return Book(name, author, image, url, sourceName, desc, lastUpdateTime, lastUpdataChapter, null, chapters)
    }

    override suspend fun getContent(url: String): String? {
        return runCatching {
            val response = api.getUrlContent(url)
            val firstDocument = Jsoup.parse(response)
            // 一章小说内容是分页的，这里获取页数
            val pageCount = firstDocument.clazz("bookname").child(0).text().split("/").last().removeSuffix(")").toInt()
            (1..pageCount).map {
                defaultKoin.get<CoroutineScope>().async {
                    var document = firstDocument
                    if (it > 1) {
                        val pageUrl = url.replace(".html", "_${it}.html")
                        document = Jsoup.parse(api.getUrlContent(pageUrl))
                    }
                    parseChapterContent(document)
                }
            }.awaitAll().joinToString("\n")
        }.getOrNull()
    }

    private fun parseChapterContent(document: Document): String {
        return document.id("ccc").childNodes().filterIsInstance<TextNode>().map { it.text() }.joinToString("\n")
    }
}

@Serializable
private data class SearchBody(val searchkey: String, val searchtype: String = "articlename")