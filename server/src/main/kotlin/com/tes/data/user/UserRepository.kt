package com.tes.data.user

import com.tes.domain.user.User

/**
 * Repository abstraction for accessing and storing user data.
 */
interface UserRepository {

    /**
     * Creates a new user in the database.
     * @param firstName First name of the user.
     * @param lastName Last name of the user.
     * @param email Email address of the user (unique).
     * @param passwordHash Password to be stored in the database (hashed).
     * @return The newly created [User], including its generated database ID.
     */
    fun createUser(
        firstName: String,
        lastName: String,
        email: String,
        passwordHash: String
    ): User

    /**
     * Looks up a user by email address.
     * @param email Email address to search for.
     * @return The matching [User] if found or "null" if no user exists for this email.
     */
    fun findByEmail(email: String): User?

    /**
     * Looks up a user by ID.
     * @param id User ID to search for.
     * @return The matching [User] if found or "null" if no user exists with this ID.
     */
    fun findById(id: Int): User?
}
