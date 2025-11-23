package com.tes

import com.tes.api.auth.authRoutes
import com.tes.api.health.healthRoutes
import com.tes.config.DatabaseConfig
import com.tes.config.DatabaseInitializer
import com.tes.data.shared.DbUserRepository
import com.tes.domain.auth.AuthService
import com.tes.domain.health.HealthService
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import java.time.Instant

/**
 * Stores the timestamp when the server process was started.
 */
val serverStartTime: Instant = Instant.now()

/**
 * Entry point of the server.
 * Starts an embedded Ktor server using Netty on port 8080 and
 * delegates the application configuration to the [module] function.
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
 */
fun Application.module() {
    // Configure JSON serialization and content negotiation
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

    // Create repositories and domain services
    val userRepository = DbUserRepository(database)
    val authService = AuthService(userRepository)
    val healthService = HealthService()

    // Register HTTP routes for the API
    routing {
        healthRoutes(healthService, serverStartTime)
        authRoutes(authService, userRepository)
    }
}
