package com.tes.data.user

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

/**
 * Ktorm mapping for the "users" database table.
 * The column names must match the "CREATE TABLE users" definition exactly.
 * This table is used by repositories to read and write user records.
 */
object UsersTable : Table<Nothing>("users") {
    val id = int("id").primaryKey()             // primary key of the user
    val firstName = varchar("first_name")       // users first name
    val lastName = varchar("last_name")         // users last name
    val email = varchar("email")                // users email address (unique)
    val passwordHash = varchar("password_hash") // hashed user password (not yet)
}
