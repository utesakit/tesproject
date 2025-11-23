package com.tes.domain.auth

import com.tes.data.auth.RefreshTokenRepository
import com.tes.data.user.UserRepository
import com.tes.domain.user.User
import org.mindrot.jbcrypt.BCrypt

/**
 * Provides authentication-related business logic.
 * Handles validation of registration data, checks email availability, verifies user credentials,
 * password hashing and JWT token generation.
 */
class AuthService(
    private val userRepository: UserRepository,
    private val tokenService: TokenService,
    private val refreshTokenRepository: RefreshTokenRepository
) {

    /**
     * Validates the data of a registration request.
     * @param firstName Users first name
     * @param lastName Users last name
     * @param email Users email address
     * @param password Users password
     * @throws ValidationException If any field is empty, the email is invalid or the password is too short.
     */
    fun validateRegistration(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ) {
        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank()) {
            throw ValidationException("All fields must be non-empty.")
        }

        if (!isValidEmail(email)) {
            throw ValidationException("Invalid email format.")
        }

        if (password.length < 8) {
            throw ValidationException("Password must be at least 8 characters long.")
        }
    }

    /**
     * Checks whether the given email address is already in use.
     * @param email Email address to check.
     * @throws EmailAlreadyExistsException If a user with the given email already exists.
     */
    fun checkEmailAvailability(email: String) {
        val existing = userRepository.findByEmail(email)
        if (existing != null) {
            throw EmailAlreadyExistsException("Email is already registered.")
        }
    }

    /**
     * Authenticates a user with email and password.
     * Verifies the password using BCrypt hashing.
     * @param email Users email address
     * @param password Users password (plain text)
     * @return The authenticated [User] instance.
     * @throws AuthenticationException If the email is unknown or the password is incorrect.
     */
    fun authenticate(email: String, password: String): User {
        val user = userRepository.findByEmail(email)
            ?: throw AuthenticationException("Email or password is incorrect.")

        // Verify password using BCrypt
        if (!BCrypt.checkpw(password, user.passwordHash)) {
            throw AuthenticationException("Email or password is incorrect.")
        }

        return user
    }

    /**
     * Hashes a plain text password using BCrypt.
     * @param password Plain text password to hash.
     * @return BCrypt hashed password string.
     */
    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    /**
     * Generates access and refresh tokens for an authenticated user.
     * Stores the refresh token in the database for later validation.
     * @param user Authenticated user which tokens should be generated.
     * @return Pair of (accessToken, refreshToken).
     */
    fun generateTokens(user: User): Pair<String, String> {
        val accessToken = tokenService.generateAccessToken(user.id, user.email)
        val refreshToken = tokenService.generateRefreshToken(user.id)

        // Store refresh token in database
        refreshTokenRepository.saveToken(user.id, refreshToken)

        return Pair(accessToken, refreshToken)
    }

    /**
     * Refreshes an access token using a valid refresh token.
     * Validates the refresh token & checks if it exists in the database
     * and generates a new access/refresh token pair.
     * @param refreshToken The refresh token to use for renewal.
     * @return Pair of (accessToken, refreshToken) if successful.
     * @throws AuthenticationException If the refresh token is invalid or not found.
     */
    fun refreshTokens(refreshToken: String): Pair<String, String> {
        // Validate refresh token JWT
        val userId = tokenService.validateRefreshToken(refreshToken)
            ?: throw AuthenticationException("Invalid refresh token.")

        // Verify token exists in database
        val storedUserId = refreshTokenRepository.findUserIdByToken(refreshToken)
            ?: throw AuthenticationException("Refresh token not found or expired.")

        if (userId != storedUserId) {
            throw AuthenticationException("Token mismatch.")
        }

        // Get user data by ID
        val user = userRepository.findById(userId)
            ?: throw AuthenticationException("User not found.")

        // Delete old refresh token (one-time use)
        refreshTokenRepository.deleteToken(refreshToken)

        // Generate new token pair
        val accessToken = tokenService.generateAccessToken(user.id, user.email)
        val newRefreshToken = tokenService.generateRefreshToken(user.id)
        refreshTokenRepository.saveToken(user.id, newRefreshToken)

        return Pair(accessToken, newRefreshToken)
    }

    /**
     * Performs a very simple email format check.
     * @param email Email address to validate.
     * @return "true" if the email is valid, "false" otherwise.
     */
    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".") && email.length >= 5
    }
}

/**
 * Thrown when validation of user input fails.
 */
class ValidationException(message: String) : Exception(message)

/**
 * Thrown when an email address is already associated with an existing user.
 */
class EmailAlreadyExistsException(message: String) : Exception(message)

/**
 * Thrown when authentication of a user fails.
 */
class AuthenticationException(message: String) : Exception(message)
