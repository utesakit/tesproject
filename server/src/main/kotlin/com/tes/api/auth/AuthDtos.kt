package com.tes.api.auth

import kotlinx.serialization.Serializable

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
 * Request body for logging in an existing user.
 */
@Serializable
data class LoginRequest(
    val email: String,      // users email address
    val password: String    // raw password for authentication
)

/**
 * Response returned for successful registration or user lookup.
 * Contains only public, non-sensitive user information.
 */
@Serializable
data class UserResponse(
    val id: Int,            // database ID of the user
    val firstName: String,  // users first name
    val lastName: String,   // users last name
    val email: String       // users email address
)

/**
 * Generic response wrapper containing a readable message.
 */
@Serializable
data class MessageResponse(
    val message: String     // (error) message
)

/**
 * Response returned after successful login or registration.
 * Contains access token, refresh token and user information.
 */
@Serializable
data class AuthResponse(
    val accessToken: String,   // JWT access token (short-lived)
    val refreshToken: String,  // JWT refresh token (long-lived)
    val user: UserResponse     // User information
)

/**
 * Request body for refreshing an access token.
 */
@Serializable
data class RefreshTokenRequest(
    val refreshToken: String  // Refresh token to use for renewal
)

/**
 * Response returned after successful token refresh.
 */
@Serializable
data class RefreshTokenResponse(
    val accessToken: String,   // New JWT access token
    val refreshToken: String  // New JWT refresh token
)