package com.tes.data.user

import com.tes.domain.user.User
import org.ktorm.dsl.QueryRowSet

/**
 * Helper for converting database rows into [User] domain objects.
 *
 * This mapper lives in the data layer and is used by [DbUserRepository].
 * It centralises all column => field mapping logic in one place so that:
 * - changes in the "users" table structure only need to be updated here and
 * - the rest of the code can work with the domain [User] type instead of raw database rows.
 */
object UserMapper {

    /**
     * Converts a database result row into a [User] domain object.
     *
     * @param row A single result row returned by a Ktorm query on [UsersTable].
     * @return The corresponding [User] instance with all fields populated.
     */
    fun fromRow(row: QueryRowSet): User {
        return User(
            id = row[UsersTable.id]!!,
            firstName = row[UsersTable.firstName]!!,
            lastName = row[UsersTable.lastName]!!,
            email = row[UsersTable.email]!!,
            passwordHash = row[UsersTable.passwordHash]!!
        )
    }
}
