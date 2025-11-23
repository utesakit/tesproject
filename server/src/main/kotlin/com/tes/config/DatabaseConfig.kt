package com.tes.config

import org.ktorm.database.Database
// import org.ktorm.support.postgresql.PostgreSqlDialect

/**
 * Configuration for database connection.
 * Contains connection parameters and creates the database instance.
 */
object DatabaseConfig {

    /**
     * Creates and returns a database connection. => parameters from config file later!
     */
    fun createDatabase(): Database {
        return Database.connect(
            url = "jdbc:postgresql://localhost:5432/postgres", // JDBC URL of the PostgreSQL database
            driver = "org.postgresql.Driver",                  // PostgreSQL JDBC driver
            user = "postgres",                                 // database username
            password = "AndroidAppA1!",                        // database password
            // dialect = PostgreSqlDialect()                      // Ktorm PostgreSQL dialect is used
        )
    }
}