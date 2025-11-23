package com.tes.api.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond

/**
 * Extracts the user ID from the JWT access token in the Authorization header.
 * @param jwtSecret JWT secret used for validation.
 * @param jwtIssuer JWT issuer used for validation.
 * @return User ID from the token, or null if the token is invalid.
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
 * Ensures the request contains a valid JWT access token and returns the user ID.
 * Responds with 401 if authentication fails.
 * @param jwtSecret JWT secret used for validation.
 * @param jwtIssuer JWT issuer used for validation.
 * @return Authenticated user ID.
 * @throws Exception If the token is missing or invalid.
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

