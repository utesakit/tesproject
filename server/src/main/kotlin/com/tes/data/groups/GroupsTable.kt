package com.tes.data.groups

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

/**
 * Ktorm table definition for the "groups" table.
 *
 * This mapping is used by the data layer to build type-safe SQL queries.
 */
object GroupsTable : Table<Nothing>("groups") {
    val id = int("id").primaryKey()
    val name = varchar("name")
    val invitationCode = varchar("invitation_code")
    val adminId = int("admin_id")
}

/**
 * Ktorm table definition for the "group_members" table.
 *
 * This table represents the many-to-many relationship between users and groups:
 * each row links exactly one user to exactly one group.
 */
object GroupMembersTable : Table<Nothing>("group_members") {
    val id = int("id").primaryKey()
    val groupId = int("group_id")
    val userId = int("user_id")
}
