package com.tes.data.groups

import com.tes.domain.groups.Group
import com.tes.domain.groups.GroupMember

/**
 * Repository abstraction for persisting and loading groups and memberships.
 */
interface GroupRepository {

    /**
     * Creates a new group.
     * @param name Group name.
     * @param invitationCode 6-character invitation code.
     * @param adminId ID of the group creator.
     * @return The created [Group] with generated ID.
     */
    fun createGroup(name: String, invitationCode: String, adminId: Int): Group

    /**
     * Finds a group by its ID.
     * @param id Group ID.
     * @return Matching [Group] or null if not found.
     */
    fun findById(id: Int): Group?

    /**
     * Finds a group by its invitation code.
     * @param invitationCode Invitation code.
     * @return Matching [Group] or null if not found.
     */
    fun findByInvitationCode(invitationCode: String): Group?

    /**
     * Deletes a group.
     * @param id Group ID to delete.
     */
    fun deleteGroup(id: Int)

    /**
     * Adds a user as member to a group.
     * @param groupId Group ID.
     * @param userId User ID.
     * @return Created [GroupMember] instance.
     */
    fun addMember(groupId: Int, userId: Int): GroupMember

    /**
     * Removes a user from a group.
     * @param groupId Group ID.
     * @param userId User ID.
     */
    fun removeMember(groupId: Int, userId: Int)

    /**
     * Checks if a user is a member of a group.
     * @param groupId Group ID.
     * @param userId User ID.
     * @return true if user is member, false otherwise.
     */
    fun isMember(groupId: Int, userId: Int): Boolean

    /**
     * Returns all groups a user is member of.
     * @param userId User ID.
     * @return List of [Group]s.
     */
    fun findGroupsByUserId(userId: Int): List<Group>

    /**
     * Returns all members of a group.
     * @param groupId Group ID.
     * @return List of [GroupMember] entries.
     */
    fun findMembersByGroupId(groupId: Int): List<GroupMember>
}

