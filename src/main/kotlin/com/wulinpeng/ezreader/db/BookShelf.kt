package com.wulinpeng.ezreader.db

import org.jetbrains.exposed.sql.Table

object BookShelf: Table("book_shelf") {
    val bookId = varchar("bookId", 500)
    val progress = integer("progress").default(0)
    val name = varchar("name", 500)
    val author = varchar("author", 500)
    val image = varchar("image", 500).nullable()
    val desc = varchar("desc", 500).nullable()
    override val primaryKey = PrimaryKey(bookId)
}