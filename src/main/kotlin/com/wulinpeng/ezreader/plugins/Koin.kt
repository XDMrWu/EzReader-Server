package com.wulinpeng.ezreader.plugins

import io.ktor.server.application.*
import org.koin.core.Koin
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module
import org.koin.mp.KoinPlatformTools


val defaultKoin: Koin
    get() = KoinPlatformTools.defaultContext().get()

fun Application.configureKoin() {
    startKoin {
        modules(KoinModule().module)
    }
}

@Module
@ComponentScan("com.wulinpeng.ezreader")
class KoinModule