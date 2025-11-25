package com.tes.data.auth

import com.tes.domain.auth.RefreshTokenRepository
import org.ktorm.database.Database
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where

/**
 * PostgreSQL / Ktorm implementation of [RefreshTokenRepository].
 *
 * This class belongs to the data (persistence) layer and
 * is responsible for storing and looking up refresh tokens in the "refresh_tokens" table.
 *
 * Responsibilities:
 * - Insert new refresh tokens when a user logs in or registers.
 * - Look up the user ID for a given refresh token.
 * - Delete refresh tokens (=> token rotation & for logout?).
 * - Delete all tokens for a user (TODO: not implemented yet!)
 *
 * The domain layer only depends on the [RefreshTokenRepository] interface and does not need to know any SQL details.
 * Those are fully hidden here.
 *
 * @param database Shared Ktorm [Database] instance used to execute SQL queries.
 */
class DbRefreshTokenRepository(
    private val database: Database
) : RefreshTokenRepository {

    /**
     * Saves a new refresh token for a user.
     *
     * Inserts a row into the "refresh_tokens" table linking the given token string to the given user ID.
     *
     * @param userId ID of the user who owns this token.
     * @param token Refresh token string to store.
     */
    override fun saveToken(userId: Int, token: String) {
        database.insert(RefreshTokensTable) {
            set(RefreshTokensTable.userId, userId)
            set(RefreshTokensTable.token, token)
        }
    }

    /**
     * Finds the user ID associated with a refresh token.
     *
     * @param token Refresh token string to look up.
     * @return The ID of the user who owns this token or "null" if the token is not found.
     */
    override fun findUserIdByToken(token: String): Int? {
        return database
            .from(RefreshTokensTable)
            .select(RefreshTokensTable.userId)
            .where { RefreshTokensTable.token eq token }
            .map { it[RefreshTokensTable.userId] }
            .firstOrNull()
    }

    /**
     * Deletes a specific refresh token from the database.
     *
     * @param token Refresh token string to delete.
     */
    override fun deleteToken(token: String) {
        database.delete(RefreshTokensTable) {
            it.token eq token
        }
    }

    /**
     * Deletes all refresh tokens belonging to a specific user.
     * TODO: not implemented yet!
     *
     * @param userId ID of the user whose tokens should be removed.
     */
    override fun deleteAllTokensForUser(userId: Int) {
        database.delete(RefreshTokensTable) {
            it.userId eq userId
        }
    }
}
