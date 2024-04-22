package com.wulinpeng.ezreader.source.impl

import com.wulinpeng.ezreader.plugins.defaultHttpClient
import com.wulinpeng.ezreader.source.core.Book
import com.wulinpeng.ezreader.source.core.BookSource
import com.wulinpeng.ezreader.source.core.Chapter
import com.wulinpeng.ezreader.source.core.CommonApi
import de.jensklingenberg.ktorfit.Ktorfit
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import org.jsoup.nodes.TextNode
import org.koin.core.annotation.Single

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

    private fun parseBook(content: String): List<Book> {
        val document = Jsoup.parse(content)
        println(document)
        return document.getElementsByAttributeValue("id", "nr").map {
            val name = it.child(0).getElementsByTag("a").first().text()
            val author = it.child(2).text()
            val url = it.child(0).getElementsByTag("a").first().attr("href")
            Book(name, author, null, url, sourceName, null)
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
        val author = infoElement.child(1).text().removePrefix("作者：").split(" ").first()
        val image = BASE_URL + document.getElementById("fmimg").getElementsByTag("img").first().attr("src").removePrefix("/")
        val lastUpdateTime = infoElement.child(1).text().split("最后更新：").last()
        val desc = document.getElementById("maininfo").getElementById("intro").child(0).childNodes().filterIsInstance<TextNode>().map { it.text() }.joinToString("\n")
        val lastUpdataChapter = infoElement.children().last().text().removePrefix("最新章节：")
        val chapters = document.getElementById("list").child(0).children().let {
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
            val document = Jsoup.parse(response)
            // 一章小说内容是分页的，这里获取页数
            val pageCount = document.getElementsByClass("bookname").first().child(0).text().split("/").last().removeSuffix(")").toInt()
            val contents = mutableListOf<String>()
            repeat(pageCount) {
                if (it == 0) {
                    contents.add(parseChapterContent(response))
                } else {
                    val pageUrl = url.replace(".html", "_${it + 1}.html")
                    val pageContent = api.getUrlContent(pageUrl)
                    contents.add(parseChapterContent(pageContent))
                }
            }
            contents.joinToString("\n")
        }.getOrNull()
    }

    private fun parseChapterContent(content: String): String {
        val document = Jsoup.parse(content)
        return document.getElementById("ccc").childNodes().filterIsInstance<TextNode>().map { it.text() }.joinToString("\n")
    }
}

@Serializable
private data class SearchBody(val searchkey: String, val searchtype: String = "articlename")