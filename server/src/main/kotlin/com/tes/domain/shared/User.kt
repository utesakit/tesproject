package com.tes.domain.shared

/**
 * Represents a registered user.
 * This domain model is used by the business logic.
 */
data class User(
    val id: Int,              // database ID of the user
    val firstName: String,    // users first name
    val lastName: String,     // users last name
    val email: String,        // users email address (unique)
    val passwordHash: String  // hashed password stored in the database (not yet)
)