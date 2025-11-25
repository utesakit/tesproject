package com.tes.api.auth

import com.tes.domain.user.User
import kotlinx.serialization.Serializable

/**
 * Data Transfer Objects (DTOs) used by the authentication REST API.
 *
 * These classes define:
 * - Request bodies for registration, login and token refresh.
 * - Response bodies containing user information and JWT tokens.
 *
 * They act as a stable contract between server and clients and hide internal domain model or database details.
 */

/**
 * Request body for user registration.
 *
 * Sent by the client when a new user signs up.
 * The raw password is validated and hashed on the server side.
 *
 * @property firstName First name of the user.
 * @property lastName Last name of the user.
 * @property email Email address of the user (unique).
 * @property password Plain-text password supplied by the client.
 */
@Serializable
data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)

/**
 * Request body for logging in an existing user.
 *
 * @property email Email address of the user.
 * @property password Plain-text password for authentication.
 */
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * API response representing a single user.
 *
 * Contains only public, non-sensitive user information.
 *
 * @property id Unique database identifier of the user.
 * @property firstName First name of the user.
 * @property lastName Last name of the user.
 * @property email Email address of the user.
 */
@Serializable
data class UserResponse(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String
)

/**
 * Generic API response with a readable message.
 *
 * Commonly used for error responses.
 *
 * @property message Message text to be shown to the user.
 */
@Serializable
data class MessageResponse(
    val message: String
)

/**
 * API response returned after successful login or registration.
 *
 * Contains the issued token pair and public user information.
 *
 * @property accessToken Short-lived JWT access token.
 * @property refreshToken Long-lived JWT refresh token.
 * @property user Public user data for the authenticated user.
 */
@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse
)

/**
 * Request body for refreshing an access token.
 *
 * @property refreshToken Refresh token used to request a new token pair.
 */
@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

/**
 * API response returned after successful token refresh.
 *
 * @property accessToken New short-lived JWT access token.
 * @property refreshToken New long-lived JWT refresh token.
 */
@Serializable
data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String
)

/**
 * Maps a domain [User] entity to a [UserResponse] DTO.
 *
 * Keeps the API layer in control of which user fields are exposed to clients.
 */
fun User.toResponse(): UserResponse =
    UserResponse(
        id = this.id,
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email
    )
