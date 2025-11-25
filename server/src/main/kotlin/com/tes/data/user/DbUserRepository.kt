package com.tes.data.user

import com.tes.domain.user.User
import com.tes.domain.user.UserRepository
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where

/**
 * PostgreSQL / Ktorm implementation of [UserRepository].
 *
 * This class belongs to the data layer and is responsible for reading and writing user data in the "users" table.
 *
 * Responsibilities:
 * - Create new users in the database.
 * - Look up users by email or by ID.
 *
 * All higher-level validation rules and password handling are implemented in [com.tes.domain.auth.AuthService].
 * This repository focuses only on database access.
 *
 * @param database Shared Ktorm [Database] instance used to run SQL queries.
 */
class DbUserRepository(
    private val database: Database
) : UserRepository {

    /**
     * Inserts a new user into the "users" table and returns the created domain object.
     *
     * Steps:
     * - Insert the new user row (first name, last name, email, password hash).
     * - Load the user again by email to retrieve the generated database ID.
     *
     * @param firstName First name of the new user.
     * @param lastName Last name of the new user.
     * @param email Email address of the new user.
     * @param passwordHash Hashed password to be stored.
     * @return The persisted [User], including its generated database ID.
     *
     * @throws IllegalStateException If the user cannot be loaded again after insertion.
     */
    override fun createUser(
        firstName: String,
        lastName: String,
        email: String,
        passwordHash: String
    ): User {
        // Insert a new row into the "users" table.
        database.insert(UsersTable) {
            set(UsersTable.firstName, firstName)
            set(UsersTable.lastName, lastName)
            set(UsersTable.email, email)
            set(UsersTable.passwordHash, passwordHash)
        }

        // Load the inserted user by email so we can return it with its generated ID.
        return database
            .from(UsersTable)
            .select()
            .where { UsersTable.email eq email }
            .map { UserMapper.fromRow(it) }
            .firstOrNull()
            ?: throw IllegalStateException("User could not be loaded after insert.")
    }

    /**
     * Retrieves a user by their email address, if one exists.
     *
     * @param email Email address to search for.
     * @return The matching [User] or "null" if no user with this email exists.
     */
    override fun findByEmail(email: String): User? {
        return database
            .from(UsersTable)
            .select()
            .where { UsersTable.email eq email }
            .map { UserMapper.fromRow(it) }
            .firstOrNull()
    }

    /**
     * Retrieves a user by their unique ID, if one exists.
     *
     * @param id User ID to search for.
     * @return The matching [User] or "null" if no user with this ID exists.
     */
    override fun findById(id: Int): User? {
        return database
            .from(UsersTable)
            .select()
            .where { UsersTable.id eq id }
            .map { UserMapper.fromRow(it) }
            .firstOrNull()
    }
}
