package com.tes.data.user

import com.tes.api.auth.UserResponse
import com.tes.domain.user.User
import org.ktorm.dsl.QueryRowSet

/**
 * Maps between database rows, domain models, and API DTOs for users.
 */
object UserMapper {

    /**
     * Converts a database row into a [User] domain object.
     * @param row Result row returned by Ktorm.
     * @return The corresponding [User] instance.
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

    /**
     * Converts a [User] domain object into a [UserResponse] DTO that can be safely sent to the client.
     * @param user Domain user that should be exposed via the API.
     * @return A [UserResponse] containing only public user data.
     */
    fun toResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id,
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email
        )
    }
}
