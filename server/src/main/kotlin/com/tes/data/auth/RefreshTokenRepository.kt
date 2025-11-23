package com.tes.data.auth

/**
 * Repository abstraction for managing refresh tokens.
 * Handles storage and retrieval of refresh tokens used for JWT authentication.
 */
interface RefreshTokenRepository {
    /**
     * Stores a new refresh token for a user.
     * @param userId ID of the user who owns this token.
     * @param token The refresh token string to store.
     */
    fun saveToken(userId: Int, token: String)

    /**
     * Retrieves the user ID associated with a refresh token.
     * @param token The refresh token to look up.
     * @return The user ID if the token exists otherwise null.
     */
    fun findUserIdByToken(token: String): Int?

    /**
     * Removes a refresh token from the database (logout/revocation).
     * @param token The refresh token to delete.
     */
    fun deleteToken(token: String)

    /**
     * Removes all refresh tokens for a specific user.
     * @param userId ID of the user whose tokens should be removed.
     */
    fun deleteAllTokensForUser(userId: Int)
}