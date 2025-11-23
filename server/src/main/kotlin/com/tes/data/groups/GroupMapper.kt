package com.tes.data.groups

import com.tes.domain.groups.Group
import com.tes.domain.groups.GroupMember
import org.ktorm.dsl.QueryRowSet

/**
 * Maps between database rows and group domain models.
 */
object GroupMapper {

    /**
     * Maps a database row to a [Group].
     * @param row Ktorm query row.
     * @return Corresponding [Group] instance.
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
     * Maps a database row to a [GroupMember].
     * @param row Ktorm query row.
     * @return Corresponding [GroupMember] instance.
     */
    fun memberFromRow(row: QueryRowSet): GroupMember {
        return GroupMember(
            id = row[GroupMembersTable.id]!!,
            groupId = row[GroupMembersTable.groupId]!!,
            userId = row[GroupMembersTable.userId]!!
        )
    }
}

