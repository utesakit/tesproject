package com.tes.domain.auth

import com.tes.domain.user.UserRepository
import com.tes.domain.user.User
import org.mindrot.jbcrypt.BCrypt

/**
 * Provides authentication-related business logic for the application.
 *
 * This service belongs to the domain layer and is completely independent from HTTP or JSON details.
 *
 * It is used by the API layer to:
 * - Validate registration input (names, email, password).
 * - Ensure email addresses are unique.
 * - Hash and verify passwords using BCrypt.
 * - Authenticate users during login.
 * - Coordinate with [TokenService] and [RefreshTokenRepository] to issue and refresh JWT access/refresh tokens.
 */
class AuthService(
    private val userRepository: UserRepository,
    private val tokenService: TokenService,
    private val refreshTokenRepository: RefreshTokenRepository
) {

    /**
     * Validates the data of a user registration request.
     *
     * Checks that:
     * - all fields are non-empty,
     * - the email has a valid basic format,
     * - the password has a minimum length.
     *
     * @param firstName User's first name.
     * @param lastName User's last name.
     * @param email User's email address.
     * @param password User's plaintext password.
     *
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
     *
     * @param email Email address to check.
     *
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
     *
     * The method:
     * - loads the user by email,
     * - compares the provided plaintext password with the stored BCrypt hash,
     * - returns the [User] if the credentials are correct.
     *
     * For security reasons, the same error message is used for "email unknown" and "wrong password"!
     *
     * @param email Users email address.
     * @param password Users plaintext password.
     * @return The authenticated [User] instance.
     *
     * @throws AuthenticationException If the email is unknown or the password is incorrect.
     */
    fun authenticate(email: String, password: String): User {
        val user = userRepository.findByEmail(email)
            ?: throw AuthenticationException("Email or password is incorrect.")

        // Verify password using BCrypt (compares plaintext against the stored hash).
        if (!BCrypt.checkpw(password, user.passwordHash)) {
            throw AuthenticationException("Email or password is incorrect.")
        }

        return user
    }

    /**
     * Hashes a plaintext password using BCrypt.
     *
     * The returned hash is what gets stored in the database.
     *
     * @param password Plaintext password to hash.
     * @return BCrypt hashed password string.
     */
    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    /**
     * Generates access and refresh tokens for an authenticated user.
     *
     * The method:
     * - creates a short-lived access token and a long-lived refresh token,
     * - stores the refresh token in the database so it can be validated later,
     * - returns both tokens as a pair.
     *
     * @param user Authenticated user for whom the tokens should be generated.
     * @return Pair of (accessToken, refreshToken).
     */
    fun generateTokens(user: User): Pair<String, String> {
        val accessToken = tokenService.generateAccessToken(user.id, user.email)
        val refreshToken = tokenService.generateRefreshToken(user.id)

        // Store the refresh token in the database so it can be validated and revoked.
        refreshTokenRepository.saveToken(user.id, refreshToken)

        return Pair(accessToken, refreshToken)
    }

    /**
     * Refreshes an access token using a valid refresh token.
     *
     * Steps:
     * - Validate the refresh tokens signature, issuer and expiration.
     * - Look up the token in the database to ensure it is still stored.
     * - Ensure the user ID inside the token matches the one in the database.
     * - Load the corresponding user.
     * - Delete the old refresh token (one-time use!).
     * - Generate and store a new access/refresh token pair.
     *
     * @param refreshToken The refresh token to use for renewal.
     * @return Pair of (newAccessToken, newRefreshToken) if successful.
     *
     * @throws AuthenticationException If the refresh token is invalid, unknown or linked to a missing user.
     */
    fun refreshTokens(refreshToken: String): Pair<String, String> {
        // Validate refresh token JWT and extract the user ID from it.
        val userId = tokenService.validateRefreshToken(refreshToken)
            ?: throw AuthenticationException("Invalid refresh token.")

        // Verify the token exists in the database.
        val storedUserId = refreshTokenRepository.findUserIdByToken(refreshToken)
            ?: throw AuthenticationException("Refresh token not found or expired.")

        // Check that the user IDs match.
        if (userId != storedUserId) {
            throw AuthenticationException("Token mismatch.")
        }

        // Load the user from the user repository.
        val user = userRepository.findById(userId)
            ?: throw AuthenticationException("User not found.")

        // Delete the old refresh token to enforce one-time use.
        refreshTokenRepository.deleteToken(refreshToken)

        // Generate and store a new token pair.
        val accessToken = tokenService.generateAccessToken(user.id, user.email)
        val newRefreshToken = tokenService.generateRefreshToken(user.id)
        refreshTokenRepository.saveToken(user.id, newRefreshToken)

        return Pair(accessToken, newRefreshToken)
    }

    /**
     * Performs a very simple email format check.
     *
     * TODO: This is not a full email validation.
     * It only checks for the presence of '@' and '.' and a minimal length!
     *
     * @param email Email address to validate.
     * @return "true" if the email looks valid, "false" otherwise.
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
 * Thrown when an email address is already associated with an existing user (during registration).
 */
class EmailAlreadyExistsException(message: String) : Exception(message)

/**
 * Thrown when authentication of a user fails.
 */
class AuthenticationException(message: String) : Exception(message)
