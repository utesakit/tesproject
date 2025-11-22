package com.tes.domain

/**
 * Domain model representing a user that can register and log in.
 */
data class User(
    val id: Int,              // database ID of the user
    val firstName: String,    // users first name
    val lastName: String,     // users last name
    val email: String,        // users email address (unique)
    val passwordHash: String  // hashed password stored in the database (not yet)
)
