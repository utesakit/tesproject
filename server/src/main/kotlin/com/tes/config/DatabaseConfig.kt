package com.tes.config

import org.ktorm.database.Database
// import org.ktorm.support.postgresql.PostgreSqlDialect

/**
 * Central configuration for database access.
 * Provides a factory method that creates the Ktorm [Database] instance used by the server.
 */
object DatabaseConfig {

    /**
     * Creates and returns a [Database] connection.
     * For now the connection settings are hard-coded.
     * Later they should be loaded from a configuration file / environment variables!
     * @return A connected [Database] instance.
     */
    fun createDatabase(): Database {
        return Database.connect(
            url = "jdbc:postgresql://localhost:5432/postgres", // JDBC URL of the PostgreSQL database
            driver = "org.postgresql.Driver",                  // PostgreSQL JDBC driver
            user = "postgres",                                 // Database username
            password = "AndroidAppA1!",                        // Database password
            // dialect = PostgreSqlDialect()                   // Ktorm PostgreSQL dialect
        )
    }
}