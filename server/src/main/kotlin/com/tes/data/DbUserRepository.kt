package com.tes.data

import com.tes.domain.User
import org.ktorm.database.Database
import org.ktorm.dsl.*

/**
 * UserRepository implementation using Ktorm with PostgreSQL.
 */
class DbUserRepository(
    private val database: Database
) : UserRepository {

    override fun createUser(
        firstName: String,
        lastName: String,
        email: String,
        passwordHash: String
    ): User {

        return try {
            // Insert new user into the database
            database.insert(UsersTable) {
                set(UsersTable.firstName, firstName)
                set(UsersTable.lastName, lastName)
                set(UsersTable.email, email)
                set(UsersTable.passwordHash, passwordHash)
            }

            // Load the inserted user by email
            database
                .from(UsersTable)
                .select()
                .where { UsersTable.email eq email }
                .map { row ->
                    User(
                        id = row[UsersTable.id]!!,
                        firstName = row[UsersTable.firstName]!!,
                        lastName = row[UsersTable.lastName]!!,
                        email = row[UsersTable.email]!!,
                        passwordHash = row[UsersTable.passwordHash]!!
                    )
                }
                .firstOrNull()
                ?: error("User could not be loaded after insert.")
        } catch (e: Exception) {
            // Log and rethrow the exception
            e.printStackTrace()
            throw e
        }
    }

    override fun findByEmail(email: String): User? {

        return try {
            // Find the first user with the given email
            database
                .from(UsersTable)
                .select()
                .where { UsersTable.email eq email }
                .map { row ->
                    User(
                        id = row[UsersTable.id]!!,
                        firstName = row[UsersTable.firstName]!!,
                        lastName = row[UsersTable.lastName]!!,
                        email = row[UsersTable.email]!!,
                        passwordHash = row[UsersTable.passwordHash]!!
                    )
                }
                .firstOrNull()
        } catch (e: Exception) {
            // Log and rethrow the exception
            e.printStackTrace()
            throw e
        }
    }
}
