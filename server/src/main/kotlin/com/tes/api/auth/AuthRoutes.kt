package com.tes.api.auth

import com.tes.data.user.UserMapper
import com.tes.data.user.UserRepository
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

            // Hash the raw password before saving it to the database
            val passwordHash = authService.hashPassword(request.password)

            // Create user in the database with hashed password.
            val user = userRepository.createUser(
                firstName = request.firstName,
                lastName = request.lastName,
                email = request.email,
                passwordHash = passwordHash
            )

            // Generate a fresh access/refresh token pair for the new user
            val (accessToken, refreshToken) = authService.generateTokens(user)

            // Respond with 201 Created and return tokens plus user information as JSON
            call.respond(
                HttpStatusCode.Created,
                AuthResponse(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    user = UserMapper.toResponse(user)
                )
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

            // Generate a new access/refresh token pair for the authenticated user
            val (accessToken, refreshToken) = authService.generateTokens(user)

            // Respond with 200 OK and return tokens plus user information as JSON
            call.respond(
                HttpStatusCode.OK,
                AuthResponse(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    user = UserMapper.toResponse(user)
                )
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

    post("/auth/refresh") {
        try {
            val request = call.receive<RefreshTokenRequest>()

            // Refresh the token pair using the provided refresh token
            val (accessToken, refreshToken) = authService.refreshTokens(request.refreshToken)

            // Respond with 200 OK and return the new token pair as JSON
            call.respond(
                HttpStatusCode.OK,
                RefreshTokenResponse(
                    accessToken = accessToken,
                    refreshToken = refreshToken
                )
            )
        } catch (e: AuthenticationException) {
            call.respond(
                HttpStatusCode.Unauthorized,
                MessageResponse(e.message ?: "Token refresh failed.")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                MessageResponse("Internal server error during token refresh.")
            )
        }
    }
}
