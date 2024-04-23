package com.wulinpeng.ezreader.source.impl

import com.wulinpeng.ezreader.plugins.defaultHttpClient
import com.wulinpeng.ezreader.route.defaultJson
import com.wulinpeng.ezreader.source.core.Book
import com.wulinpeng.ezreader.source.core.BookSource
import com.wulinpeng.ezreader.source.core.CommonApi
import de.jensklingenberg.ktorfit.Ktorfit
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.core.annotation.Single

/**
 * 纵横中文网，仅支持热搜词
 */
@Single
class ZonghengSource: BookSource {
    private val BASE_URL = "https://search.zongheng.com/"
    private val api = Ktorfit.Builder().httpClient(defaultHttpClient).baseUrl("$BASE_URL").build().create<CommonApi>()


    override val sourceName: String = "纵横中文网"

    override val priority: Int = 0

    override suspend fun searchBook(bookName: String) = emptyList<Book>()

    override suspend fun getBookDetail(url: String) = null

    override suspend fun getContent(url: String): String? = null

    override suspend fun getHotWords(): List<String> {
        return runCatching {
            defaultJson.decodeFromString<JsonObject>(api.getUrlContent("https://search.zongheng.com/search/suggest"))
                .get("data")?.jsonObject?.get("books")?.jsonArray?.map {
                    it.jsonPrimitive.content
                } ?: emptyList()
        }.getOrNull() ?: emptyList()
    }
}