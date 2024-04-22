package com.wulinpeng.reader.source.core

import java.io.Serializable


/**
 * author：wulinpeng
 * date：2021/7/18 22:03
 * desc:
 */
data class Book(val name: String,
                val author: String,
                val image: String?,
                val url: String,
                val source: String,
                val desc: String?,
                val lastUpdateTime: String? = null,
                val lastUpdateChapter: String? = null,
                val category: String? = null,
                val chapterList: List<Chapter>? = null): Serializable
data class Update(val time: String, val title: String): Serializable