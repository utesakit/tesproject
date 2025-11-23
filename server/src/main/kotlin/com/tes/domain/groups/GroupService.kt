package com.tes.domain.groups

import com.tes.data.groups.GroupRepository
import com.tes.data.user.UserRepository
import java.util.*

/**
 * Business logic for managing groups (create, join, leave, delete, remove members).
 */
class GroupService(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository
) {

    /**
     * Creates a new group with a unique invitation code.
     * The creator becomes admin and initial member.
     * @param name Group name.
     * @param adminId ID of the creating user.
     * @return The created [Group].
     * @throws GroupException If name is blank or admin does not exist.
     */
    fun createGroup(name: String, adminId: Int): Group {
        if (name.isBlank()) {
            throw GroupException("Group name must not be empty.")
        }

        // Ensure admin user exists TODO: unnötig, wegen JWT?
        val admin = userRepository.findById(adminId)
            ?: throw GroupException("User does not exist.")

        // Generate unique 6-character invitation code
        val invitationCode = generateUniqueInvitationCode()

        // Persist group (admin is added as member in repository)
        return groupRepository.createGroup(name, invitationCode, adminId)
    }

    /**
     * Lets a user join a group via invitation code.
     * @param invitationCode Group invitation code.
     * @param userId ID of the joining user.
     * @return The joined [Group].
     * @throws GroupException If code is invalid or user is already a member.
     */
    fun joinGroup(invitationCode: String, userId: Int): Group {
        if (invitationCode.length != 6) {
            throw GroupException("Invitation code must be exactly 6 characters long.")
        }

        // Look up group by invitation code
        val group = groupRepository.findByInvitationCode(invitationCode)
            ?: throw GroupException("Invalid invitation code.")

        // Ensure user exists TODO: unnötig, wegen JWT?
        val user = userRepository.findById(userId)
            ?: throw GroupException("User does not exist.")

        // Prevent duplicate membership
        if (groupRepository.isMember(group.id, userId)) {
            throw GroupException("User is already a member of this group.")
        }

        // Add user to group
        groupRepository.addMember(group.id, userId)

        return group
    }

    /**
     * Lets a user leave a group.
     * @param groupId Group ID.
     * @param userId User ID.
     * @throws GroupException If group does not exist, user is not a member or the admin tries to leave.
     */
    fun leaveGroup(groupId: Int, userId: Int) {
        val group = groupRepository.findById(groupId)
            ?: throw GroupException("Group does not exist.")

        // Admin must delete the group instead of leaving
        if (group.adminId == userId) {
            throw GroupException("Group creator cannot leave the group. Please delete the group instead.")
        }

        // Ensure user is a member of the group
        if (!groupRepository.isMember(groupId, userId)) {
            throw GroupException("User is not a member of this group.")
        }

        // Remove user from group
        groupRepository.removeMember(groupId, userId)
    }

    /**
     * Deletes a group. Only the admin may delete.
     * @param groupId Group ID.
     * @param userId ID of the requesting user.
     * @throws GroupException If group does not exist or user is not the admin.
     */
    fun deleteGroup(groupId: Int, userId: Int) {
        val group = groupRepository.findById(groupId)
            ?: throw GroupException("Group does not exist.")

        // Enforce admin-only delete
        if (group.adminId != userId) {
            throw GroupException("Only the group creator can delete the group.")
        }

        // Delete group; memberships are removed via cascade
        groupRepository.deleteGroup(groupId)
    }

    /**
     * Removes a member from a group. Only the admin may remove members.
     * @param groupId Group ID.
     * @param memberUserId ID of the member to remove.
     * @param adminUserId ID of the acting admin.
     * @throws GroupException If group does not exist, actor is not admin or target user is not a member.
     */
    fun removeMember(groupId: Int, memberUserId: Int, adminUserId: Int) {
        val group = groupRepository.findById(groupId)
            ?: throw GroupException("Group does not exist.")

        // Enforce admin-only remove
        if (group.adminId != adminUserId) {
            throw GroupException("Only the group creator can remove members.")
        }

        // Admin cannot remove themselves
        if (memberUserId == adminUserId) {
            throw GroupException("Group creator cannot remove themselves. Please delete the group instead.")
        }

        // Ensure target user is a member
        if (!groupRepository.isMember(groupId, memberUserId)) {
            throw GroupException("User is not a member of this group.")
        }

        // Remove member from group
        groupRepository.removeMember(groupId, memberUserId)
    }

    /**
     * Returns all groups a user is a member of.
     * @param userId User ID.
     * @return List of [Group]s for the user.
     */
    fun getUserGroups(userId: Int): List<Group> {
        return groupRepository.findGroupsByUserId(userId)
    }

    /**
     * Generates a unique 6-character alphanumeric invitation code.
     * @return Unique invitation code.
     * @throws GroupException If a unique code cannot be generated.
     */
    private fun generateUniqueInvitationCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        var code: String
        var attempts = 0
        do {
            code = (1..6)
                .map { chars[Random().nextInt(chars.length)] }
                .joinToString("")
            attempts++
            if (attempts > 100) {
                throw GroupException("Could not generate a unique invitation code.")
            }
        } while (groupRepository.findByInvitationCode(code) != null)

        return code
    }
}

/**
 * Thrown when a group operation fails.
 */
class GroupException(message: String) : Exception(message)

