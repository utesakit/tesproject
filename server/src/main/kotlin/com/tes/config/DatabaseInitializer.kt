package com.tes.config

import org.ktorm.database.Database

/**
 * Initializes and updates the database schema used by the server.
 * This object is called on server startup to make sure all required tables exist before any requests are handled.
 */
object DatabaseInitializer {

    /**
     * Ensures that the database schema is present.
     * Currently this creates the "users" table if it does not already exist.
     * @param database Active database connection used to run the DDL statements.
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
