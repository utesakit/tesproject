package com.tes.config

import org.ktorm.database.Database

/**
 * Initializes and updates the database schema used by the server.
 * This object is called on server startup to make sure all required tables exist before any requests are handled.
 */
object DatabaseInitializer {

    /**
     * Ensures that the database schema is present.
     * Creates the "users" and "refresh_tokens" tables if they do not already exist.
     * @param database Active database connection used to run the DDL statements.
     */
    fun initDatabase(database: Database) {
        database.useConnection { connection ->
            connection.createStatement().use { statement ->
                // Create users table if it does not exist yet
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

                // Create refresh_tokens table if it does not exist yet
                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS refresh_tokens (
                        id      SERIAL PRIMARY KEY,
                        user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                        token   VARCHAR(500) UNIQUE NOT NULL
                    );
                    """.trimIndent()
                )

                // Create index on user_id for fast lookups by user (TODO: not so important)
                statement.executeUpdate(
                    """
                    CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id 
                    ON refresh_tokens(user_id);
                    """.trimIndent()
                )
            }
        }
    }
}
