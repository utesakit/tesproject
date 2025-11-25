package com.tes.config

import org.ktorm.database.Database
// import org.ktorm.support.postgresql.PostgreSqlDialect

/**
 * Central place to configure and create the database connection.
 *
 * This object belongs to the infrastructure layer and is responsible for providing
 * a ready-to-use Ktorm [Database] instance that can be used by all repository implementations.
 *
 * TODO: Currently it connects to a local PostgreSQL database using hard-coded credentials.
 */
object DatabaseConfig {

    /**
     * Creates a new [Database] connection to the PostgreSQL server.
     *
     * The returned [Database] instance is used by the data layer to execute SQL queries via Ktorm.
     *
     * @return A configured Ktorm [Database] instance ready for queries.
     */
    fun createDatabase(): Database {
        return Database.connect(
            url = "jdbc:postgresql://localhost:5432/postgres", // TODO: JDBC URL of the PostgreSQL database
            driver = "org.postgresql.Driver",                  // PostgreSQL JDBC driver
            user = "postgres",                                 // TODO: Database username.
            password = "AndroidAppA1!"                         // TODO: Database password.
            // dialect = PostgreSqlDialect()                   // Optional: Ktorm PostgreSQL dialect.
        )
    }
}
