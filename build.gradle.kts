val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project


repositories {
    mavenCentral()
    google()
}

val exposed_version = "0.49.0"

plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
    id("io.ktor.plugin") version "2.3.10"
    id("com.google.devtools.ksp") version "1.9.23-1.0.20"
    id("de.jensklingenberg.ktorfit") version "1.13.0"
}

group = "com.wulinpeng.ezreader"
version = "0.0.1"

application {
    mainClass.set("com.wulinpeng.ezreader.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

// 依赖 ksp 生成的代码
kotlin {
    sourceSets {
        main.configure {
            kotlin.srcDir("build/generated/ksp/main/kotlin")
        }
    }
}

dependencies {
    // Ktor
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-client-core-jvm")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Ktorfit
    implementation("de.jensklingenberg.ktorfit:ktorfit-lib:1.13.0")
    ksp("de.jensklingenberg.ktorfit:ktorfit-ksp:1.13.0")

    // DB
    implementation("org.jetbrains.exposed", "exposed-core", exposed_version)
    implementation("org.jetbrains.exposed", "exposed-dao", exposed_version)
    implementation("org.jetbrains.exposed", "exposed-jdbc", exposed_version)
    implementation("mysql:mysql-connector-java:8.0.33")

    // Koin
    implementation("io.insert-koin:koin-core:3.5.6")
    implementation("io.insert-koin:koin-annotations:1.3.1")
    ksp("io.insert-koin:koin-ksp-compiler:1.3.1")

    // Jsoup
    implementation("org.jsoup:jsoup:1.11.2")

    implementation("ch.qos.logback:logback-classic:$logback_version")
}
