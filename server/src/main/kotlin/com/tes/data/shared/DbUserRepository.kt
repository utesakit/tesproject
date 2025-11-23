package com.tes.data.shared

import com.tes.domain.shared.User
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where

/**
 * [UserRepository] implementation by a PostgreSQL database using Ktorm.
 * This class encapsulates all database access related to users so that
 * the rest of the application does not depend on Ktorm or SQL directly.
 */
class DbUserRepository(
    private val database: Database
) : UserRepository {

    /**
     * Inserts a new user into the "users" table and returns the created domain object.
     * @param firstName First name of the new user.
     * @param lastName Last name of the new user.
     * @param email Email address of the new user (unique).
     * @param passwordHash Password to be stored (hashed).
     * @return The persisted [User] including its generated database ID.
     * @throws IllegalStateException If the user cannot be loaded after insertion.
     */
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

        // Load the inserted user by email (including its generated ID)
        return database
            .from(UsersTable)
            .select()
            .where { UsersTable.email eq email }
            .map { UserMapper.fromRow(it) }
            .firstOrNull()
            ?: throw IllegalStateException("User could not be loaded after insert.")
    }

    /**
     * Retrieves a user by email if it exists.
     * @param email Email address to search for.
     * @return The matching [User], or "null" if no user with this email exists.
     */
    override fun findByEmail(email: String): User? {
        return database
            .from(UsersTable)
            .select()
            .where { UsersTable.email eq email }
            .map { UserMapper.fromRow(it) }
            .firstOrNull()
    }
}