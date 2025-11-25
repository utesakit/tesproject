package com.tes

import com.tes.api.auth.authRoutes
import com.tes.api.groups.groupRoutes
import com.tes.api.health.healthRoutes
import com.tes.config.DatabaseConfig
import com.tes.config.DatabaseInitializer
import com.tes.data.auth.DbRefreshTokenRepository
import com.tes.data.groups.DbGroupRepository
import com.tes.data.user.DbUserRepository
import com.tes.domain.auth.AuthService
import com.tes.domain.auth.TokenService
import com.tes.domain.groups.GroupService
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
 * Timestamp captured once when the server process starts.
 *
 * The health endpoint uses this value to calculate how long the server has been running (uptime in seconds).
 */
val serverStartTime: Instant = Instant.now()

/**
 * JVM entry point of the server application.
 *
 * Starts an embedded Netty HTTP server on port 8080 and delegates all application wiring to [Application.module].
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
 * Primary Ktor application module and composition root.
 *
 * This function is called by the embedded Netty server and is responsible for:
 * - Installing Ktor plugins (e.g. JSON content negotiation).
 * - Creating the database connection and initializing the schema.
 * - Reading security-related configuration (JWT secret and issuer).
 * - Constructing repositories and domain services (auth, groups, health).
 * - Registering all HTTP routes that make up the REST API.
 */
fun Application.module() {
    // Install JSON (kotlinx.serialization) for request and response bodies.
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
            }
        )
    }

    // Create the database connection and ensure that all required tables exist.
    val database = DatabaseConfig.createDatabase()
    DatabaseInitializer.initDatabase(database)

    // TODO: In production, secrets and issuer MUST come from environment variables / configuration file!
    val jwtSecret = System.getenv("JWT_SECRET") ?: "secret-jwt-key-change-in-production-min-32-chars"
    val jwtIssuer = System.getenv("JWT_ISSUER") ?: "http://localhost:8080"

    // Construct repository implementations (data layer).
    val userRepository = DbUserRepository(database)
    val refreshTokenRepository = DbRefreshTokenRepository(database)
    val groupRepository = DbGroupRepository(database)

    // TokenService is responsible for creating and validating JWT tokens (access & refresh tokens).
    val tokenService = TokenService(
        jwtSecret = jwtSecret,
        jwtIssuer = jwtIssuer
    )

    // AuthService coordinates user repo, token service and refresh token repo
    val authService = AuthService(
        userRepository = userRepository,
        tokenService = tokenService,
        refreshTokenRepository = refreshTokenRepository
    )

    // GroupService contains the business logic for creating and managing groups.
    val groupService = GroupService(
        groupRepository = groupRepository,
        // userRepository = userRepository (siehe Groupservice.kt)
    )

    // HealthService builds a simple health response including server uptime.
    val healthService = HealthService()

    // Register all HTTP routes of the REST API.
    routing {
        // Health endpoint: returns status and uptime information.
        healthRoutes(healthService, serverStartTime)

        // Authentication endpoints: register, log in, refresh tokens
        authRoutes(authService, userRepository)

        // Group endpoints: create, join, leave, delete groups, remove members
        groupRoutes(groupService, jwtSecret, jwtIssuer)
    }
}
