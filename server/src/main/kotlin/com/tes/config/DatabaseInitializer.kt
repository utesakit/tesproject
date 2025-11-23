package com.tes.config

import org.ktorm.database.Database

/**
 * Handles database schema initialization and migrations.
 * Ensures that all required tables exist on server startup.
 */
object DatabaseInitializer {

    /**
     * Initializes the database schema on server startup.
     * Ensures that the "users" table exists.
     * @param database the database connection to initialize
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
}
