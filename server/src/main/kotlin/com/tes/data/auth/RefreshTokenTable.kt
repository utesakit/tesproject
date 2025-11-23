package com.tes.data.auth

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

/**
 * Ktorm mapping for the "refresh_tokens" database table.
 * Stores refresh tokens used for JWT token renewal.
 * Each token is associated with a user_id and contains a unique token string.
 */
object RefreshTokensTable : Table<Nothing>("refresh_tokens") {
    val id = int("id").primaryKey()              // primary key of the refresh token
    val userId = int("user_id")                  // foreign key to users table
    val token = varchar("token")                 // unique refresh token string
}
