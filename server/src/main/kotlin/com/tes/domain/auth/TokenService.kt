package com.tes.domain.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import java.util.Date

/**
 * Service responsible for creating and validating JWT access and refresh tokens.
 *
 * This class encapsulates all logic related to JSON Web Tokens (JWT) and is part of the domain layer.
 * It does not know anything about HTTP or Ktor, it simply produces and validates token strings.
 *
 * Main responsibilities:
 * - Build signed JWTs containing the user ID and other claims.
 * - Set appropriate expiration times for access and refresh tokens.
 * - Validate existing tokens and extract the embedded user ID.
 *
 * @param jwtSecret Secret key used to sign and verify tokens (must be kept private!).
 * @param jwtIssuer Expected issuer claim stored in the tokens (usually the server URL!).
 * @param accessTokenExpirationMinutes Lifetime of access tokens in minutes.
 * @param refreshTokenExpirationDays Lifetime of refresh tokens in days.
 */
class TokenService(
    private val jwtSecret: String,
    private val jwtIssuer: String,
    private val accessTokenExpirationMinutes: Long = 15,  // Access tokens expire after 15 minutes
    private val refreshTokenExpirationDays: Long = 30     // Refresh tokens expire after 30 days
) {

    /**
     * Generates a short-lived access token for an authenticated user.
     *
     * Access tokens are used to authorize API calls. They contain:
     * - the user ID as the token subject,
     * - the users email address,
     * - a claim "type" = "access" to distinguish it from refresh tokens,
     * - an expiration time a few minutes in the future.
     *
     * @param userId ID of the authenticated user.
     * @param email Email address of the authenticated user.
     * @return A signed JWT access token as a string.
     */
    fun generateAccessToken(userId: Int, email: String): String {
        val now = Date()
        val expiration = Date(now.time + accessTokenExpirationMinutes * 60 * 1000)

        return JWT.create()
            .withIssuer(jwtIssuer)                           // Who created the token
            .withSubject(userId.toString())         // Who the token is about (user ID)
            .withClaim("email", email)        // Additional user info
            .withClaim("type", "access")      // Mark this as an access token
            .withIssuedAt(now)                     // When the token was issued
            .withExpiresAt(expiration)            // When the token will expire
            .sign(Algorithm.HMAC256(jwtSecret))              // Sign using the shared secret key
    }

    /**
     * Generates a long-lived refresh token for an authenticated user.
     *
     * Refresh tokens are used to obtain new access tokens without requiring
     * the user to log in again. They:
     * - last much longer than access tokens (days instead of minutes),
     * - contain the user ID as the subject,
     * - are marked with a claim "type" = "refresh".
     *
     * @param userId ID of the authenticated user.
     * @return A signed JWT refresh token as a string.
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
     * Validates a refresh token and extracts the user ID if it is valid.
     *
     * This method checks:
     * - the token signature (using [jwtSecret]),
     * - the issuer (must match [jwtIssuer]),
     * - the "type" claim (must be "refresh"),
     * - and the expiration time.
     *
     * If any of these checks fail, the token is considered invalid.
     *
     * @param token JWT refresh token to validate.
     * @return The user ID contained in the token if it is valid, or `null` if invalid.
     */
    fun validateRefreshToken(token: String): Int? {
        return try {
            val verifier = JWT.require(Algorithm.HMAC256(jwtSecret))
                .withIssuer(jwtIssuer)
                .withClaim("type", "refresh")
                .build()

            val decoded = verifier.verify(token)
            decoded.subject.toIntOrNull()
        } catch (_: JWTVerificationException) {
            null
        }
    }
}
