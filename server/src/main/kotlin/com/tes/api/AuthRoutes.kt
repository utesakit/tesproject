package com.tes.api

import com.tes.data.UserRepository
import com.tes.domain.User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Simple test routes for registration and login.
 * Currently without password hashing / JWT => only for testing DB integration!
 */
fun Route.authRoutes(userRepository: UserRepository) {

    post("/auth/register") {
        try {
            // Parse JSON body into RegisterRequest
            val request = call.receive<RegisterRequest>()

            // Basic validation: all fields must be non-empty
            if (request.firstName.isBlank() ||
                request.lastName.isBlank() ||
                request.email.isBlank() ||
                request.password.isBlank()
            ) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    MessageResponse("All fields must be non-empty.")
                )
                return@post
            }

            // Check if email is already registered
            val existing = userRepository.findByEmail(request.email)
            if (existing != null) {
                call.respond(
                    HttpStatusCode.Conflict,
                    MessageResponse("Email is already registered.")
                )
                return@post
            }

            // For testing: store password in plain text => will be hashed later?
            val user = userRepository.createUser(
                firstName = request.firstName,
                lastName = request.lastName,
                email = request.email,
                passwordHash = request.password
            )

            // Return created user without password
            call.respond(
                HttpStatusCode.Created,
                user.toResponse()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                MessageResponse("Internal server error during registration.")
            )
        }
    }

    post("/auth/login") {
        try {
            // Parse JSON body into LoginRequest
            val request = call.receive<LoginRequest>()

            // Look up user by email
            val user = userRepository.findByEmail(request.email)

            // Validate credentials => plain-text comparison for now?
            if (user == null || user.passwordHash != request.password) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    MessageResponse("Email or password is incorrect.")
                )
                return@post
            }

            // Login successful
            call.respond(
                HttpStatusCode.OK,
                MessageResponse("Login successful for ${user.email}.")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                MessageResponse("Internal server error during login.")
            )
        }
    }
}

/**
 * Maps a domain User to a response DTO without the password.
 */
private fun User.toResponse(): UserResponse =
    UserResponse(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email
    )
