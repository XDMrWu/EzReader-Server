package com.wulinpeng.ezreader

import com.wulinpeng.ezreader.crypto.aesSecretKey
import com.wulinpeng.ezreader.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.io.File

fun main(args: Array<String>) {
    parseArgs(args)
    embeddedServer(Netty, port = 8080, module = Application::module)
        .start(wait = true)
}

fun parseArgs(args: Array<String>) {
    if (args.isNotEmpty()) {
        DBPassword = args[0]
        aesSecretKey = args[1]
    } else {
        // 本地读取 local.properties 文件
        File("local.properties").readLines().forEach {
            val parts = it.split("=")
            when (parts[0]) {
                "db_password" -> DBPassword = parts[1]
                "aes_secret_key" -> aesSecretKey = parts[1]
            }
        }
    }
    if (DBPassword.isNullOrEmpty() || aesSecretKey.isNullOrEmpty()) {
        throw IllegalArgumentException("DB password or AES secret key is missing")
    }
}

fun Application.module() {
    configureKoin()
    configureHttpClient()
    configureRouting()
    configureDatabase()
}
