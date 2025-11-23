package com.tes.api

import com.tes.data.UserMapper
import com.tes.data.UserRepository
import com.tes.domain.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * HTTP routes for user authentication (registration and login).
 * Delegates business logic to AuthService.
 */
fun Route.authRoutes(authService: AuthService, userRepository: UserRepository) {

    post("/auth/register") {
        try {
            val request = call.receive<RegisterRequest>()

            // Validate registration data
            authService.validateRegistration(
                firstName = request.firstName,
                lastName = request.lastName,
                email = request.email,
                password = request.password
            )

            // Check email availability
            authService.checkEmailAvailability(request.email)

            // Create user (TODO: hash password before storing)
            val user = userRepository.createUser(
                firstName = request.firstName,
                lastName = request.lastName,
                email = request.email,
                passwordHash = request.password
            )

            call.respond(
                HttpStatusCode.Created,
                UserMapper.toResponse(user)
            )
        } catch (e: ValidationException) {
            call.respond(
                HttpStatusCode.BadRequest,
                MessageResponse(e.message ?: "Validation failed.")
            )
        } catch (e: EmailAlreadyExistsException) {
            call.respond(
                HttpStatusCode.Conflict,
                MessageResponse(e.message ?: "Email is already registered.")
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
            val request = call.receive<LoginRequest>()

            // Authenticate user
            val user = authService.authenticate(request.email, request.password)

            call.respond(
                HttpStatusCode.OK,
                MessageResponse("Login successful for ${user.email}.")
            )
        } catch (e: AuthenticationException) {
            call.respond(
                HttpStatusCode.Unauthorized,
                MessageResponse(e.message ?: "Authentication failed.")
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
