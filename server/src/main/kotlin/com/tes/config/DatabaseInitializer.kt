package com.tes.config

import org.ktorm.database.Database

/**
 * Initializes the database schema on application startup.
 *
 * This object belongs to the infrastructure layer and is responsible for making sure that
 * all required tables exist before the application starts andling requests.
 *
 * For each required table ("users", "refresh_tokens", "groups", "group_members")
 * it runs a "CREATE TABLE IF NOT EXISTS" statement so the application can start even on an empty database.
 */
object DatabaseInitializer {

    /**
     * Ensures all required database tables and indexes exist.
     *
     * This method is idempotent: running it multiple times is safe because
     * "CREATE TABLE IF NOT EXISTS" and "CREATE INDEX IF NOT EXISTS" only create objects that are missing.
     *
     * @param database Ktorm [Database] used to obtain a JDBC connection.
     */
    fun initDatabase(database: Database) {
        database.useConnection { connection ->
            connection.createStatement().use { statement ->
                // Users table: stores basic user account information.
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

                // Refresh tokens table: stores one or more refresh tokens per user.
                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS refresh_tokens (
                        id      SERIAL PRIMARY KEY,
                        user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                        token   VARCHAR(500) UNIQUE NOT NULL
                    );
                    """.trimIndent()
                )

                // Index for faster lookups of all tokens for a specific user. TODO: needed?
                statement.executeUpdate(
                    """
                    CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id 
                    ON refresh_tokens(user_id);
                    """.trimIndent()
                )

                // Groups table: stores data for each group.
                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS groups (
                        id              SERIAL PRIMARY KEY,
                        name            VARCHAR(100) NOT NULL,
                        invitation_code VARCHAR(6) UNIQUE NOT NULL,
                        admin_id        INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE
                    );
                    """.trimIndent()
                )

                // Group members table: many-to-many relation between users and groups.
                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS group_members (
                        id       SERIAL PRIMARY KEY,
                        group_id INTEGER NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
                        user_id  INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                        UNIQUE(group_id, user_id)
                    );
                    """.trimIndent()
                )

                // Index for faster lookups by group. TODO: needed?
                statement.executeUpdate(
                    """
                    CREATE INDEX IF NOT EXISTS idx_group_members_group_id 
                    ON group_members(group_id);
                    """.trimIndent()
                )

                // Index for faster lookups by user. TODO: needed?
                statement.executeUpdate(
                    """
                    CREATE INDEX IF NOT EXISTS idx_group_members_user_id 
                    ON group_members(user_id);
                    """.trimIndent()
                )
            }
        }
    }
}
