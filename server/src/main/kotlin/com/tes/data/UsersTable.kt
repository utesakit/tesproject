package com.tes.data

import org.ktorm.schema.*

/**
 * Ktorm mapping for the "users" table.
 * Column names must exactly match the CREATE TABLE definition.
 */
object UsersTable : Table<Nothing>("users") {

    val id = int("id").primaryKey()          // primary key of the user
    val firstName = varchar("first_name")    // users first name
    val lastName = varchar("last_name")      // users last name
    val email = varchar("email")             // users email address (unique)
    val passwordHash = varchar("password_hash") // hashed user password (not yet)
}
