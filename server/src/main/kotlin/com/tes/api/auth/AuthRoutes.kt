package com.tes.api.auth

import com.tes.domain.user.UserRepository
import com.tes.domain.auth.AuthService
import com.tes.domain.auth.AuthenticationException
import com.tes.domain.auth.EmailAlreadyExistsException
import com.tes.domain.auth.ValidationException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

/**
 * Defines all HTTP routes related to authentication and authorization.
 *
 * Endpoints:
 * - POST /auth/register: Registers a new user account.
 * - POST /auth/login: Authenticates a user and issues JWT tokens.
 * - POST /auth/refresh: Issues new tokens based on a refresh token.
 *
 * This layer:
 * - Parses incoming JSON requests into DTOs.
 * - Calls the [AuthService] to execute business logic.
 * - Translates domain exceptions into meaningful HTTP status codes and messages.
 */

/**
 * Registers all authentication routes inside the given [Route] tree.
 *
 * @param authService Business logic for authentication and token handling.
 * @param userRepository Access to user data for registration and lookup.
 */
fun Route.authRoutes(authService: AuthService, userRepository: UserRepository) {

    // POST /auth/register: register a new user and issue initial tokens
    post<RegisterRequest>("/auth/register") { request ->
        try {
            // Check basic registration data (names, email, password)
            authService.validateRegistration(
                firstName = request.firstName,
                lastName = request.lastName,
                email = request.email,
                password = request.password
            )

            // Check that the email address is not already in use.
            authService.checkEmailAvailability(request.email)

            // Hash the plain-text password before storing it
            val passwordHash = authService.hashPassword(request.password)

            // Create the user record in the database
            val user = userRepository.createUser(
                firstName = request.firstName,
                lastName = request.lastName,
                email = request.email,
                passwordHash = passwordHash
            )

            // Issue access + refresh token for the new user
            val (accessToken, refreshToken) = authService.generateTokens(user)

            // Respond with 201 Created and return tokens and user info as JSON
            call.respond(
                HttpStatusCode.Created,
                AuthResponse(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    user = user.toResponse()
                )
            )
        } catch (e: ValidationException) {
            call.respond(
                // Client sent invalid data -> 400
                HttpStatusCode.BadRequest,
                MessageResponse(e.message ?: "Registration data is not valid.")
            )
        } catch (e: EmailAlreadyExistsException) {
            // Email is already registered -> 409 Conflict
            call.respond(
                HttpStatusCode.Conflict,
                MessageResponse(e.message ?: "Email is already registered.")
            )
        } catch (e: Exception) {
            // Any unexpected error on the server side -> 500
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                MessageResponse("Internal server error during registration.")
            )
        }
    }

    // POST /auth/login: log in with email and password and issue tokens
    post<LoginRequest>("/auth/login") { request ->
        try {
            //  Authenticate user using email and password
            val user = authService.authenticate(request.email, request.password)

            // Issue a fresh access + refresh token pair
            val (accessToken, refreshToken) = authService.generateTokens(user)

            // Respond with 200 OK and return tokens and user info as JSON
            call.respond(
                HttpStatusCode.OK,
                AuthResponse(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    user = user.toResponse()
                )
            )
        } catch (e: AuthenticationException) {
            // Wrong email or password or user not allowed to log in -> 401
            call.respond(
                HttpStatusCode.Unauthorized,
                MessageResponse(e.message ?: "Authentication failed.")
            )
        } catch (e: Exception) {
            // Any unexpected error on the server side -> 500
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                MessageResponse("Internal server error during login.")
            )
        }
    }

    // POST /auth/refresh: exchange a refresh token for a new token pair
    post<RefreshTokenRequest>("/auth/refresh") { request ->
        try {
            // Validate and exchange refresh token for a new token pair
            val (accessToken, refreshToken) = authService.refreshTokens(request.refreshToken)

            // Respond with 200 OK and return the new access + refresh token
            call.respond(
                HttpStatusCode.OK,
                RefreshTokenResponse(
                    accessToken = accessToken,
                    refreshToken = refreshToken
                )
            )
        } catch (e: AuthenticationException) {
            // Refresh token is invalid, expired or revoked -> 401
            call.respond(
                HttpStatusCode.Unauthorized,
                MessageResponse(e.message ?: "Token refresh failed.")
            )
        } catch (e: Exception) {
            // Any unexpected error on the server side -> 500
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                MessageResponse("Internal server error during token refresh.")
            )
        }
    }
}
