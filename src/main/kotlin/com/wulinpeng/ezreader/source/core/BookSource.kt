package com.wulinpeng.ezreader.source.core

import org.koin.core.component.KoinComponent

interface BookSource: KoinComponent {

    val sourceName: String

    /**
     * 优先级，数字越大优先级越高
     */
    val priority: Int

    /**
     * 搜索书籍
     * List<Book> -> hasMore
     */
    suspend fun searchBook(bookName: String): List<Book> = emptyList()

    /**
     * 获取书籍详情
     */
    suspend fun getBookDetail(url: String, skipChapter: Boolean): Book? = null

    /**
     * 获取章节内容
     */
    suspend fun getContent(url: String): String? = null

    /**
     * 获取热搜词
     */
    suspend fun getHotWords(): List<String> = emptyList()
}