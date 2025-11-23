package com.tes.api.auth

import com.tes.data.shared.UserMapper
import com.tes.data.shared.UserRepository
import com.tes.domain.auth.AuthService
import com.tes.domain.auth.AuthenticationException
import com.tes.domain.auth.EmailAlreadyExistsException
import com.tes.domain.auth.ValidationException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

/**
 * Registers HTTP routes for user authentication (registration and login).
 * All business logic is delegated to [AuthService] and [UserRepository].
 * @param authService Service used for validation and authentication.
 * @param userRepository Repository used to store and load user data.
 */
fun Route.authRoutes(authService: AuthService, userRepository: UserRepository) {

    post("/auth/register") {
        try {
            val request = call.receive<RegisterRequest>()

            // Validate basic registration data (names, email, password).
            authService.validateRegistration(
                firstName = request.firstName,
                lastName = request.lastName,
                email = request.email,
                password = request.password
            )

            // Check that the email address is not already in use.
            authService.checkEmailAvailability(request.email)

            // Create user in the database. (TODO: hash password before storing)
            val user = userRepository.createUser(
                firstName = request.firstName,
                lastName = request.lastName,
                email = request.email,
                passwordHash = request.password
            )

            // Return the created user (without password) to the client.
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

            // Authenticate user based on email and password.
            val user = authService.authenticate(request.email, request.password)

            // For now just send a simple success message.
            // TODO: Later this can return a JWT and user data.
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
