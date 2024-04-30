package com.wulinpeng.ezreader.source.impl

import com.wulinpeng.ezreader.plugins.defaultHttpClient
import com.wulinpeng.ezreader.source.core.*
import de.jensklingenberg.ktorfit.Ktorfit
import org.jsoup.Jsoup
import org.koin.core.annotation.Single

/**
 * 飞卢小说,仅支持热搜词
 * 热搜词质量不高，目前先不生效
 */
//@Single
class FalooSource: BookSource {
    private val BASE_URL = "https://wap.faloo.com/"
    private val api = Ktorfit.Builder().httpClient(defaultHttpClient).baseUrl("$BASE_URL").build().create<CommonApi>()

    override val sourceName: String = "飞卢小说"

    override val priority: Int = 0

    override suspend fun getHotWords(): List<String> {
        return runCatching {
            val document = Jsoup.parse(api.getUrlContent("https://wap.faloo.com/SearchIndex.html"))
            document.clazz("div_hotsearch_tag clearfix").tags("a").map {
                it.text()
            }
        }.getOrNull() ?: emptyList()
    }
}