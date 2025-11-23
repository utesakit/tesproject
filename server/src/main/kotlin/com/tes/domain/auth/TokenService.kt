package com.tes.domain.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import java.util.Date

/**
 * Service responsible for generating and validating JWT tokens.
 * Handles both access tokens (short-lived) and refresh tokens (long-lived).
 */
class TokenService(
    private val jwtSecret: String,
    private val jwtIssuer: String,
    private val accessTokenExpirationMinutes: Long = 15,  // Access tokens expire after 15 minutes
    private val refreshTokenExpirationDays: Long = 30     // Refresh tokens expire after 30 days
) {

    /**
     * Generates a short-lived access token for authenticated users.
     * Contains user ID and email in the claims.
     * @param userId ID of the authenticated user.
     * @param email Email address of the authenticated user.
     * @return JWT access token as a string.
     */
    fun generateAccessToken(userId: Int, email: String): String {
        val now = Date()
        val expiration = Date(now.time + accessTokenExpirationMinutes * 60 * 1000)

        return JWT.create()
            .withIssuer(jwtIssuer)
            .withSubject(userId.toString())
            .withClaim("email", email)
            .withClaim("type", "access")
            .withIssuedAt(now)
            .withExpiresAt(expiration)
            .sign(Algorithm.HMAC256(jwtSecret))
    }

    /**
     * Generates a long-lived refresh token for token renewal.
     * Contains only user ID in the claims.
     * @param userId ID of the authenticated user.
     * @return JWT refresh token as a string.
     */
    fun generateRefreshToken(userId: Int): String {
        val now = Date()
        val expiration = Date(now.time + refreshTokenExpirationDays * 24 * 60 * 60 * 1000)

        return JWT.create()
            .withIssuer(jwtIssuer)
            .withSubject(userId.toString())
            .withClaim("type", "refresh")
            .withIssuedAt(now)
            .withExpiresAt(expiration)
            .sign(Algorithm.HMAC256(jwtSecret))
    }

    /**
     * Validates and extracts user ID from a refresh token.
     * @param token JWT refresh token to validate.
     * @return User ID if token is valid, null otherwise.
     */
    fun validateRefreshToken(token: String): Int? {
        return try {
            val verifier = JWT.require(Algorithm.HMAC256(jwtSecret))
                .withIssuer(jwtIssuer)
                .withClaim("type", "refresh")
                .build()

            val decoded = verifier.verify(token)
            decoded.subject.toIntOrNull()
        } catch (e: JWTVerificationException) {
            null
        }
    }
}
