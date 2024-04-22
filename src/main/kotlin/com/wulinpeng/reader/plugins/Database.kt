package com.wulinpeng.reader.plugins

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager

var DBPassword = ""

fun Application.configureDatabase() {
    val dbName = "ezreader"
    val db = Database.connect("jdbc:mysql://localhost:3306/$dbName",
        driver = "com.mysql.cj.jdbc.Driver",
        user = "root", password = DBPassword)
    TransactionManager.defaultDatabase = db
}