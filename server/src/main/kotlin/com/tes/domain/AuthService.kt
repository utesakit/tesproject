package com.tes.domain

import com.tes.data.UserRepository

/**
 * Service layer responsible for authentication business logic.
 * Handles user registration and login validation.
 */
class AuthService(
    private val userRepository: UserRepository
) {

    /**
     * Validates registration request data.
     * @param firstName user's first name
     * @param lastName user's last name
     * @param email user's email address
     * @param password user's password
     * @throws ValidationException if validation fails
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
     * Checks if an email is already registered.
     * @param email email to check
     * @throws EmailAlreadyExistsException if email is already registered
     */
    fun checkEmailAvailability(email: String) {
        val existing = userRepository.findByEmail(email)
        if (existing != null) {
            throw EmailAlreadyExistsException("Email is already registered.")
        }
    }

    /**
     * Authenticates a user with email and password.
     * @param email user's email address
     * @param password user's password
     * @return the authenticated User
     * @throws AuthenticationException if authentication fails
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
     * Simple email validation (basic format check).
     * @param email email to validate
     * @return true if email format is valid
     */
    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".") && email.length >= 5
    }
}

/**
 * Exception thrown when validation fails.
 */
class ValidationException(message: String) : Exception(message)

/**
 * Exception thrown when email is already registered.
 */
class EmailAlreadyExistsException(message: String) : Exception(message)

/**
 * Exception thrown when authentication fails.
 */
class AuthenticationException(message: String) : Exception(message)

