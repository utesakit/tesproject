package com.tes.data.groups

import com.tes.domain.groups.Group
import com.tes.domain.groups.GroupMember
import org.ktorm.dsl.QueryRowSet

/**
 * Helper responsible for converting database rows into group-related domain objects.
 *
 * This mapper lives in the data layer and is used by the group repository implementation.
 * It keeps all column => field mapping logic in one central place so
 * that any change in the table structure only needs to be updated here.
 */
object GroupMapper {

    /**
     * Converts a database row into a [Group] domain object.
     *
     * @param row Ktorm query row returned from a query on [GroupsTable].
     * @return Corresponding [Group] instance with all fields populated.
     */
    fun fromRow(row: QueryRowSet): Group {
        return Group(
            id = row[GroupsTable.id]!!,
            name = row[GroupsTable.name]!!,
            invitationCode = row[GroupsTable.invitationCode]!!,
            adminId = row[GroupsTable.adminId]!!
        )
    }

    /**
     * Converts a database row into a [GroupMember] domain object.
     *
     * @param row Ktorm query row returned from a query on [GroupMembersTable].
     * @return Corresponding [GroupMember] instance with all fields populated.
     */
    fun memberFromRow(row: QueryRowSet): GroupMember {
        return GroupMember(
            id = row[GroupMembersTable.id]!!,
            groupId = row[GroupMembersTable.groupId]!!,
            userId = row[GroupMembersTable.userId]!!
        )
    }
}
