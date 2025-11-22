package com.tes

import com.tes.api.healthRoutes
import com.tes.domain.HealthService
import com.tes.data.InMemoryHealthRepository

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

import java.time.Instant

// Captures the moment the application process starts.
val serverStartTime: Instant = Instant.now()

fun main() {
    // Starts an embedded Netty server.
    embeddedServer(
        Netty,
        port = 8080,
        host = "0.0.0.0"
    ) {
        module() // Ktor's application module
    }.start(wait = true) // "wait = true" keeps the JVM running until manually stopped.
}

/**
 * Main Ktor application module.
 */
fun Application.module() {

    // Install JSON content negotiation using kotlinx.serialization
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                // ignoreUnknownKeys = true
            }
        )
    }

    val healthRepo = InMemoryHealthRepository() // creates health-data repository (no real DB)
    val healthService = HealthService(healthRepo)

    routing {
        healthRoutes(healthService, serverStartTime)
    }
}