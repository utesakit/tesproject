package com.tes.domain.user

/**
 * Abstraction for persistence operations related to users.
 *
 * This interface belongs to the domain layer and describes what the
 * application needs from user storage without tying it to any concrete database.
 *
 * It allows the domain layer to:
 * - Create new users.
 * - Look up users by email.
 * - Look up users by ID.
 *
 * A concrete implementation is in the data layer and can be replaced without changing the business logic.
 */
interface UserRepository {

    /**
     * Creates a new user in the underlying data store.
     *
     * @param firstName First name of the user.
     * @param lastName Last name of the user.
     * @param email Email address of the user (must be unique).
     * @param passwordHash Hashed password to be stored in the database.
     * @return The newly created [User] including its generated database ID.
     */
    fun createUser(
        firstName: String,
        lastName: String,
        email: String,
        passwordHash: String
    ): User

    /**
     * Looks up a user by email address.
     *
     * @param email Email address to search for.
     * @return The matching [User] if found or "null" if no user exists with this email.
     */
    fun findByEmail(email: String): User?

    /**
     * Looks up a user by its unique ID.
     *
     * @param id User ID to search for.
     * @return The matching [User] if found or "null" if no user exists with this ID.
     */
    fun findById(id: Int): User?
}
