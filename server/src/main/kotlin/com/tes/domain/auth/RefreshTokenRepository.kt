package com.tes.domain.auth

/**
 * Abstraction for storing and looking up refresh tokens.
 *
 * This interface lives in the domain layer and describes what the authentication logic needs from refresh token storage,
 * without tying it to any particular database or technology.
 *
 * A concrete implementation is provided in the data layer and can be replaced without changing the business logic.
 */
interface RefreshTokenRepository {

    /**
     * Stores a new refresh token for a user.
     *
     * @param userId ID of the user who owns this token.
     * @param token The refresh token string to store.
     */
    fun saveToken(userId: Int, token: String)

    /**
     * Retrieves the user ID associated with a refresh token.
     *
     * @param token The refresh token to look up.
     * @return The user ID if the token exists and is known or "null" if the token is not found.
     */
    fun findUserIdByToken(token: String): Int?

    /**
     * Removes a single refresh token from the underlying store.
     *
     * @param token The refresh token to delete.
     */
    fun deleteToken(token: String)

    /**
     * Removes all refresh tokens belonging to a specific user.
     * TODO: not implemented yet!
     *
     * @param userId ID of the user whose tokens should be removed.
     */
    fun deleteAllTokensForUser(userId: Int)
}
