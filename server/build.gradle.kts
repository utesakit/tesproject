plugins {
    kotlin("jvm") version "2.0.21" // Kotlin JVM plugin for compiling Kotlin code
    kotlin("plugin.serialization") version "2.0.21" // Serialization-Plugin
    application // Adds support for "application { mainClass = ... }"
}

repositories {
    mavenCentral() // Central Maven repository (most JVM libs are hosted here)
}

// Centralized version definitions for readability & maintainability
val ktorVersion = "2.3.7"
val ktormVersion = "4.0.0"
val postgresDriverVersion = "42.7.4"

dependencies {
    // Ktor Server Dependencies
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.6")

    // (Future) Database Support
    implementation("org.ktorm:ktorm-core:$ktormVersion")
    implementation("org.postgresql:postgresql:$postgresDriverVersion")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

    // Ktor JSON-Serialization (kotlinx.serialization)
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3") // optional
}

application {
    // Entry point for your Ktor application (generated ApplicationKt)
    mainClass.set("com.tes.ApplicationKt")
}

tasks.test {
    // Enable modern JUnit testing
    useJUnitPlatform()
}

kotlin {
    // Set JVM toolchain to Java 21
    jvmToolchain(21)
} //t