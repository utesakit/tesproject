package com.tes.data.auth

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

/**
 * Ktorm table definition for the "refresh_tokens" table.
 *
 * Each row in this table represents a single refresh token that belongs to a specific user.
 * This mapping is used by the data layer to build type-safe SQL queries for storing and looking up refresh tokens.
 */
object RefreshTokensTable : Table<Nothing>("refresh_tokens") {
    val id = int("id").primaryKey()
    val userId = int("user_id")
    val token = varchar("token")
}
