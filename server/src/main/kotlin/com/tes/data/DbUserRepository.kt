package com.tes.data

import com.tes.domain.User
import org.ktorm.database.Database
import org.ktorm.dsl.*

/**
 * UserRepository implementation using Ktorm with PostgreSQL.
 */
class DbUserRepository(
    private val database: Database
) : UserRepository {

    override fun createUser(
        firstName: String,
        lastName: String,
        email: String,
        passwordHash: String
    ): User {
        // Insert new user into the database
        database.insert(UsersTable) {
            set(UsersTable.firstName, firstName)
            set(UsersTable.lastName, lastName)
            set(UsersTable.email, email)
            set(UsersTable.passwordHash, passwordHash)
        }

        // Load the inserted user by email
        return database
            .from(UsersTable)
            .select()
            .where { UsersTable.email eq email }
            .map { UserMapper.fromRow(it) }
            .firstOrNull()
            ?: throw IllegalStateException("User could not be loaded after insert.")
    }

    override fun findByEmail(email: String): User? {
        return database
            .from(UsersTable)
            .select()
            .where { UsersTable.email eq email }
            .map { UserMapper.fromRow(it) }
            .firstOrNull()
    }
}
