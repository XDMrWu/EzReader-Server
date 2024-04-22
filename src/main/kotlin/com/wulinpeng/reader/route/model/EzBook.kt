package com.wulinpeng.reader.route.model

import com.wulinpeng.reader.route.generateId
import com.wulinpeng.reader.source.core.Book
import com.wulinpeng.reader.source.core.Chapter
import kotlinx.serialization.Serializable

@Serializable
data class EzBook(val bookId: String, val name: String, val author: String, val image: String?, val desc: String?,
                  val lastUpdateTime: String?,
                  val lastUpdateChapter: String?,
                  val category: String?,
                  val chapterList: List<EzChapter>?) {
    companion object {
        fun fromBook(book: Book): EzBook {
            return EzBook(generateId(book.source, book.url), book.name, book.author, book.image, book.desc,
                book.lastUpdateTime, book.lastUpdateChapter, book.category, book.chapterList?.map { EzChapter.fromChapter(it) })
        }
    }
}

@Serializable
data class EzChapter(val chapterId: String, val title: String) {
    companion object {
        fun fromChapter(chapter: Chapter): EzChapter {
            return EzChapter(generateId(chapter.source, chapter.url), chapter.title)
        }
    }
}