package com.tes.api

import kotlinx.serialization.Serializable

// TODO: Name von AuthDtos ändern

/**
 * Request body for user registration.
 */
@Serializable
data class RegisterRequest(
    val firstName: String,  // users first name
    val lastName: String,   // users last name
    val email: String,      // users email address (must be unique)
    val password: String    // raw password => will be hashed later?
)

/**
 * Request body for user login.
 */
@Serializable
data class LoginRequest(
    val email: String,      // users email address
    val password: String    // raw password for authentication
)

/**
 * Response sent back after a successful registration or user fetch.
 */
@Serializable
data class UserResponse(
    val id: Int,            // database ID of the user
    val firstName: String,  // users first name
    val lastName: String,   // users last name
    val email: String       // users email address
)

/**
 * Generic response with a simple message.
 */
@Serializable
data class MessageResponse(
    val message: String     // human-readable status / error message
)
