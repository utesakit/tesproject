package com.tes

import com.tes.api.healthRoutes
import com.tes.api.authRoutes
import com.tes.domain.HealthService
import com.tes.data.InMemoryHealthRepository
import com.tes.data.DbUserRepository

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.ktorm.database.Database
// import org.ktorm.support.postgresql.PostgreSqlDialect

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

    val database = Database.connect(
        url = "jdbc:postgresql://localhost:5432/postgres", // JDBC URL of the PostgreSQL database
        driver = "org.postgresql.Driver",                  // PostgreSQL JDBC driver
        user = "postgres",                                 // database username
        password = "AndroidAppA1!"                         // database password
        // dialect = PostgreSqlDialect()                   // can be enabled if Ktorm PostgreSQL dialect is used
    )

    initDatabase(database)

    // Repository for user-related database operations.
    val userRepository = DbUserRepository(database)

    // In-memory health repository for Health-API (no real DB behind it).
    val healthRepo = InMemoryHealthRepository()
    val healthService = HealthService(healthRepo)

    // Configure HTTP routes.
    routing {
        healthRoutes(healthService, serverStartTime)
        authRoutes(userRepository)
    }
}

/**
 * Initializes the database schema on server startup.
 * Ensures that the "users" table exists.
 */
fun initDatabase(database: Database) {
    database.useConnection { connection ->
        connection.createStatement().use { statement ->
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS users (
                    id            SERIAL PRIMARY KEY,
                    first_name    VARCHAR(100) NOT NULL,
                    last_name     VARCHAR(100) NOT NULL,
                    email         VARCHAR(255) UNIQUE NOT NULL,
                    password_hash VARCHAR(255) NOT NULL
                );
                """.trimIndent()
            )
        }
    }
}
