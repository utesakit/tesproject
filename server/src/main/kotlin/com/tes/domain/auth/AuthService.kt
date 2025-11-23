package com.tes.domain.auth

import com.tes.data.shared.UserRepository
import com.tes.domain.shared.User

/**
 * Provides authentication-related business logic.
 * Handles validation of registration data, checks email availability and verifies user credentials during login.
 */
class AuthService(
    private val userRepository: UserRepository
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
     * @param email Users email address
     * @param password Users password
     * @return The authenticated [User] instance.
     * @throws AuthenticationException If the email is unknown or the password is incorrect.
     */
    fun authenticate(email: String, password: String): User {
        val user = userRepository.findByEmail(email)
            ?: throw AuthenticationException("Email or password is incorrect.")

        // TODO: Replace with proper password hashing comparison
        if (user.passwordHash != password) {
            throw AuthenticationException("Email or password is incorrect.")
        }

        return user
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

