package com.tes.data.auth

import org.ktorm.database.Database
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where

/**
 * [RefreshTokenRepository] implementation using PostgreSQL database with Ktorm.
 * Manages refresh token storage and retrieval for JWT authentication.
 */
class DbRefreshTokenRepository(
    private val database: Database
) : RefreshTokenRepository {

    /**
     * Saves a new refresh token for a user.
     * Inserts the token into the database with the associated user ID.
     */
    override fun saveToken(userId: Int, token: String) {
        database.insert(RefreshTokensTable) {
            set(RefreshTokensTable.userId, userId)
            set(RefreshTokensTable.token, token)
        }
    }

    /**
     * Finds the user ID associated with a refresh token.
     * Returns null if the token does not exist.
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
     * Used when a user logs out or a token is revoked.
     */
    override fun deleteToken(token: String) {
        database.delete(RefreshTokensTable) {
            it.token eq token
        }
    }

    /**
     * Deletes all refresh tokens for a specific user.
     * Useful for logout-all functionality or account security measures.
     * TODO: FÜR SPÄTER
     */
    override fun deleteAllTokensForUser(userId: Int) {
        database.delete(RefreshTokensTable) {
            it.userId eq userId
        }
    }
}