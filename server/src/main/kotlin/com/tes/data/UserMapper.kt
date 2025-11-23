package com.tes.data

import com.tes.api.UserResponse
import com.tes.domain.User
import org.ktorm.dsl.QueryRowSet

/**
 * Mapper for converting between domain models and DTOs/data structures.
 */
object UserMapper {

    /**
     * Maps a database row to a User domain object.
     * @param row the database query row
     * @return the mapped User object
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
     * Maps a User domain object to a UserResponse DTO.
     * @param user the domain User object
     * @return the mapped UserResponse DTO
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

