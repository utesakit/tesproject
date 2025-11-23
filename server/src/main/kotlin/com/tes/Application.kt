package com.tes

import com.tes.api.healthRoutes
import com.tes.api.authRoutes
import com.tes.domain.HealthService
import com.tes.data.DbUserRepository
import com.tes.config.DatabaseConfig
import com.tes.config.DatabaseInitializer

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json


import java.time.Instant

// Stores the timestamp when the server process was started.
val serverStartTime: Instant = Instant.now()

fun main() {

    // Start embedded Netty HTTP server with the Ktor application module.
    embeddedServer(
        Netty,
        port = 8080,
        host = "0.0.0.0"
    ) {
        module()
    }.start(wait = true)
}

/**
 * Main Ktor application module.
 * Registers plugins, connects to the database and configures routes.
 */
fun Application.module() {

    // Install JSON content negotiation using kotlinx.serialization.
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                // ignoreUnknownKeys = true
            }
        )
    }

    // Initialize database connection and schema.
    val database = DatabaseConfig.createDatabase()
    DatabaseInitializer.initDatabase(database)

    // Repository for user-related database operations.
    val userRepository = DbUserRepository(database)

    // Health service (no database needed for health checks => online domain logic).
    val healthService = HealthService()

    // Configure HTTP routes.
    routing {
        healthRoutes(healthService, serverStartTime)
        authRoutes(userRepository)
    }
}
