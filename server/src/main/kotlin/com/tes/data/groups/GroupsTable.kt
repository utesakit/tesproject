package com.tes.data.groups

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

/**
 * Ktorm mapping for the "groups" table.
 * Column names must match the CREATE TABLE definition.
 */
object GroupsTable : Table<Nothing>("groups") {
    val id = int("id").primaryKey()                 // Group primary key
    val name = varchar("name")                      // Group name
    val invitationCode = varchar("invitation_code") // 6-char invitation code
    val adminId = int("admin_id")                   // ID of the group creator (admin)
}

/**
 * Ktorm mapping for the "group_members" table.
 * Stores user memberships in groups.
 */
object GroupMembersTable : Table<Nothing>("group_members") {
    val id = int("id").primaryKey() // Membership primary key
    val groupId = int("group_id")   // Group ID
    val userId = int("user_id")     // User ID
}

