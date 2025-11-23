package com.tes.data.groups

import com.tes.domain.groups.Group
import com.tes.domain.groups.GroupMember
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.dsl.eq

/**
 * [GroupRepository] implementation by PostgreSQL with Ktorm.
 * Encapsulates all group-related database access.
 */
class DbGroupRepository(
    private val database: Database
) : GroupRepository {

    override fun createGroup(name: String, invitationCode: String, adminId: Int): Group {
        // Insert new group
        database.insert(GroupsTable) {
            set(it.name, name)
            set(it.invitationCode, invitationCode)
            set(it.adminId, adminId)
        }

        // Reload group by invitation code (includes generated ID)
        val group = database
            .from(GroupsTable)
            .select()
            .where { GroupsTable.invitationCode eq invitationCode }
            .map { GroupMapper.fromRow(it) }
            .firstOrNull()
            ?: throw IllegalStateException("Failed to load group after insert.")

        // Add creator as initial member
        addMember(group.id, adminId)

        return group
    }

    override fun findById(id: Int): Group? {
        return database
            .from(GroupsTable)
            .select()
            .where { GroupsTable.id eq id }
            .map { GroupMapper.fromRow(it) }
            .firstOrNull()
    }

    override fun findByInvitationCode(invitationCode: String): Group? {
        return database
            .from(GroupsTable)
            .select()
            .where { GroupsTable.invitationCode eq invitationCode }
            .map { GroupMapper.fromRow(it) }
            .firstOrNull()
    }

    override fun deleteGroup(id: Int) {
        database.delete(GroupsTable) {
            it.id eq id
        }
    }

    override fun addMember(groupId: Int, userId: Int): GroupMember {
        // Insert new membership
        database.insert(GroupMembersTable) {
            set(it.groupId, groupId)
            set(it.userId, userId)
        }

        // Reload membership (includes generated ID)
        return database
            .from(GroupMembersTable)
            .select()
            .where {
                (GroupMembersTable.groupId eq groupId) and (GroupMembersTable.userId eq userId)
            }
            .map { GroupMapper.memberFromRow(it) }
            .firstOrNull()
            ?: throw IllegalStateException("Failed to load member after insert.")
    }

    override fun removeMember(groupId: Int, userId: Int) {
        database.delete(GroupMembersTable) {
            (it.groupId eq groupId) and (it.userId eq userId)
        }
    }

    override fun isMember(groupId: Int, userId: Int): Boolean {
        return database
            .from(GroupMembersTable)
            .select()
            .where {
                (GroupMembersTable.groupId eq groupId) and (GroupMembersTable.userId eq userId)
            }
            .map { GroupMapper.memberFromRow(it) }
            .firstOrNull() != null
    }

    override fun findGroupsByUserId(userId: Int): List<Group> {
        return database
            .from(GroupsTable)
            .innerJoin(GroupMembersTable, GroupsTable.id eq GroupMembersTable.groupId)
            .select()
            .where { GroupMembersTable.userId eq userId }
            .map { GroupMapper.fromRow(it) }
    }

    override fun findMembersByGroupId(groupId: Int): List<GroupMember> {
        return database
            .from(GroupMembersTable)
            .select()
            .where { GroupMembersTable.groupId eq groupId }
            .map { GroupMapper.memberFromRow(it) }
    }
}

