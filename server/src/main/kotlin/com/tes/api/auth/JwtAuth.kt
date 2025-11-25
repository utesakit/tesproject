package com.tes.api.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond

/**
 * Utility functions for handling JWT-based authentication in Ktor routes.
 *
 * These helpers:
 * - Read the "Authorization: Bearer <token>" header from an incoming HTTP request.
 * - Validate the access tokens signature and basic claims (issuer, type).
 * - Extract the authenticated uses ID from the tokens `subject claim.
 *
 * For routes that require authentication use [requireAuthenticatedUserId] to:
 * - Abort the request with "401 Unauthorized" if the token is missing or invalid.
 * - Obtain the ID of the currently authenticated user otherwise.
 */

/**
 * Extracts the user ID from a JWT access token in the `Authorization` header.
 *
 * Expects an "Authorization: Bearer <token>" header.
 * The token is verified using [jwtSecret] and [jwtIssuer] and must contain the claim "type = "access"".
 * On success the user ID is read from the tokens subject field and parsed as [Int].
 *
 * @param call The current Ktor application call.
 * @param jwtSecret Secret key used to verify the tokens signature.
 * @param jwtIssuer Expected issuer of the token.
 * @return The user ID from the tokes subject field or "null" if the header
 *         is missing, invalid, or the subject is not a valid integer.
 */
fun extractUserIdFromToken(
    call: io.ktor.server.application.ApplicationCall,
    jwtSecret: String,
    jwtIssuer: String
): Int? {
    val authHeader = call.request.headers["Authorization"]
        ?: return null

    if (!authHeader.startsWith("Bearer ")) {
        return null
    }

    val token = authHeader.removePrefix("Bearer ").trim()

    return try {
        val verifier = JWT.require(Algorithm.HMAC256(jwtSecret))
            .withIssuer(jwtIssuer)
            .withClaim("type", "access")
            .build()

        val decoded = verifier.verify(token)
        decoded.subject.toIntOrNull()
    } catch (e: JWTVerificationException) {
        null
    }
}

/**
 * Ensures that the current request is authenticated and returns the user ID.
 *
 * Reads and validates the JWT access token from the "Authorization" header of [call] using [extractUserIdFromToken].
 * If the token is missing or invalid, this function sends a "401 Unauthorized" response and
 * throws an [Exception] to stop further processing.
 *
 * @param call The current Ktor application call.
 * @param jwtSecret Secret key used to verify the tokens signature.
 * @param jwtIssuer Name of the component that issued the token.
 * @return The authenticated user ID.
 *
 * @throws Exception After a "401 Unauthorized" response if authentication fails.
 */
suspend fun requireAuthenticatedUserId(
    call: io.ktor.server.application.ApplicationCall,
    jwtSecret: String,
    jwtIssuer: String
): Int {
    val userId = extractUserIdFromToken(call, jwtSecret, jwtIssuer)
        ?: run {
            call.respond(
                HttpStatusCode.Unauthorized,
                MessageResponse("Invalid or missing JWT token.")
            )
            throw Exception("Unauthorized")
        }
    return userId
}
