package com.tes.data.groups

import com.tes.domain.groups.Group
import com.tes.domain.groups.GroupMember
import com.tes.domain.groups.GroupRepository
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.delete
import org.ktorm.dsl.from
import org.ktorm.dsl.innerJoin
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import org.ktorm.dsl.eq

/**
 * PostgreSQL / Ktorm implementation of [GroupRepository].
 *
 * This class belongs to the data layer and encapsulates all database operations related to groups & group memberships.
 *
 * Responsibilities:
 * - Create and delete groups.
 * - Add and remove group members.
 * - Query which groups a user belongs to.
 * - Load all members of a given group.
 *
 * Business rules such as permissions and validation are implemented in [com.tes.domain.groups.GroupService].
 *
 * @param database Shared Ktorm [Database] instance used to execute SQL queries.
 */
class DbGroupRepository(
    private val database: Database
) : GroupRepository {

    /**
     * Inserts a new group into the database and returns the created domain object.
     *
     * Steps:
     * - Insert a new row into the "groups" table.
     * - Reload the group by its invitation code to obtain the generated ID.
     * - Add the creator as the initial member of the group.
     *
     * @param name Group name.
     * @param invitationCode Unique code that users can use to join the group.
     * @param adminId ID of the user who will be the group admin.
     * @return The created [Group], including its generated database ID.
     *
     * @throws IllegalStateException If the group cannot be reloaded after insertion.
     */
    override fun createGroup(name: String, invitationCode: String, adminId: Int): Group {
        // Insert a new group row into the "groups" table.
        database.insert(GroupsTable) {
            set(it.name, name)
            set(it.invitationCode, invitationCode)
            set(it.adminId, adminId)
        }

        // Reload the group by invitation code so we can return it with its generated ID.
        val group = database
            .from(GroupsTable)
            .select()
            .where { GroupsTable.invitationCode eq invitationCode }
            .map { GroupMapper.fromRow(it) }
            .firstOrNull()
            ?: throw IllegalStateException("Failed to load group after insert.")

        // Add the creator as the initial member of the group.
        addMember(group.id, adminId)

        return group
    }

    /**
     * Finds a group by its unique ID.
     *
     * @param id Group ID to search for.
     * @return The matching [Group] or "null" if no group with this ID exists.
     */
    override fun findById(id: Int): Group? {
        return database
            .from(GroupsTable)
            .select()
            .where { GroupsTable.id eq id }
            .map { GroupMapper.fromRow(it) }
            .firstOrNull()
    }

    /**
     * Finds a group by its invitation code.
     *
     * @param invitationCode Invitation code to search for.
     * @return The matching [Group] or "null2 if no group exists with this code.
     */
    override fun findByInvitationCode(invitationCode: String): Group? {
        return database
            .from(GroupsTable)
            .select()
            .where { GroupsTable.invitationCode eq invitationCode }
            .map { GroupMapper.fromRow(it) }
            .firstOrNull()
    }

    /**
     * Deletes a group by its ID.
     *
     * @param id ID of the group to delete.
     */
    override fun deleteGroup(id: Int) {
        database.delete(GroupsTable) {
            it.id eq id
        }
    }

    /**
     * Adds a user as a member of a group.
     *
     * Steps:
     * - Insert a row into the "group_members" table.
     * - Reload the membership entry to return it with its generated ID.
     *
     * @param groupId ID of the group to join.
     * @param userId ID of the user who should become a member.
     * @return The created [GroupMember] entry representing this membership.
     *
     * @throws IllegalStateException If the membership cannot be reloaded after insertion.
     */
    override fun addMember(groupId: Int, userId: Int): GroupMember {
        // Insert a new membership row for this (group, user) combination.
        database.insert(GroupMembersTable) {
            set(it.groupId, groupId)
            set(it.userId, userId)
        }

        // Reload the membership so we can return it with its generated ID.
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

    /**
     * Removes a user from a group.
     *
     * @param groupId ID of the group.
     * @param userId ID of the user to remove from the group.
     */
    override fun removeMember(groupId: Int, userId: Int) {
        database.delete(GroupMembersTable) {
            (it.groupId eq groupId) and (it.userId eq userId)
        }
    }

    /**
     * Checks whether a user is currently a member of a group.
     *
     * @param groupId ID of the group.
     * @param userId ID of the user.
     * @return "true" if a membership entry exists, "false" otherwise.
     */
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

    /**
     * Returns all groups a given user is a member of.
     *
     * @param userId ID of the user.
     * @return List of [Group]s the user belongs to.
     */
    override fun findGroupsByUserId(userId: Int): List<Group> {
        return database
            .from(GroupsTable)
            .innerJoin(GroupMembersTable, GroupsTable.id eq GroupMembersTable.groupId)
            .select()
            .where { GroupMembersTable.userId eq userId }
            .map { GroupMapper.fromRow(it) }
    }

    /**
     * Returns all membership entries for a given group.
     *
     * @param groupId ID of the group.
     * @return List of [GroupMember] entries for all users in this group.
     */
    override fun findMembersByGroupId(groupId: Int): List<GroupMember> {
        return database
            .from(GroupMembersTable)
            .select()
            .where { GroupMembersTable.groupId eq groupId }
            .map { GroupMapper.memberFromRow(it) }
    }
}
