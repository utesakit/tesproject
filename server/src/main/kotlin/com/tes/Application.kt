package com.tes

import com.tes.api.authRoutes
import com.tes.api.healthRoutes
import com.tes.config.DatabaseConfig
import com.tes.config.DatabaseInitializer
import com.tes.data.DbUserRepository
import com.tes.domain.AuthService
import com.tes.domain.HealthService
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.time.Instant

/**
 * Stores the timestamp when the server process was started.
 */
val serverStartTime: Instant = Instant.now()

/**
 * Application entry point.
 */
fun main() {
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
    // Install JSON content negotiation using kotlinx.serialization
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
            }
        )
    }

    // Initialize database connection and schema
    val database = DatabaseConfig.createDatabase()
    DatabaseInitializer.initDatabase(database)

    // Initialize repositories and services
    val userRepository = DbUserRepository(database)
    val authService = AuthService(userRepository)
    val healthService = HealthService()

    // Configure HTTP routes
    routing {
        healthRoutes(healthService, serverStartTime)
        authRoutes(authService, userRepository)
    }
}
