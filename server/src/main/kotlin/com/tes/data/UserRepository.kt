package com.tes.data

import com.tes.domain.User

/**
 * Abstraction for user data access.
 * Used by the authentication service.
 */
interface UserRepository {

    /**
     * Creates a new user in the database.
     * @param firstName users first name
     * @param lastName users last name
     * @param email users email (must be unique)
     * @param passwordHash hashed user password
     * @return the created User including its database ID
     */
    fun createUser(
        firstName: String,
        lastName: String,
        email: String,
        passwordHash: String
    ): User

    /**
     * Finds a user by email address.
     * @param email email to look up
     * @return the User if found, or null otherwise
     */
    fun findByEmail(email: String): User?
}
