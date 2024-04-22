package com.wulinpeng.ezreader.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    val routeConfigures: List<EzReaderRouteConfigure> = defaultKoin.getAll()

    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        route("ezreader") {
            routeConfigures.forEach {
                it.config(this)
            }
        }
    }
}

interface EzReaderRouteConfigure {
    fun config(route: Route)
}