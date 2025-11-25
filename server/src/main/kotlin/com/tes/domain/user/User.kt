package com.tes.domain.user

/**
 * Domain model representing an application user.
 *
 * This class is part of the domain layer and is used internally on the server side to represent users.
 * It should never be sent directly to clients because it contains the password hash.
 *
 * @property id Database-generated user ID.
 * @property firstName Users first name.
 * @property lastName Users last name.
 * @property email Unique email address used for login.
 * @property passwordHash Hashed password.
 */
data class User(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val passwordHash: String
)
