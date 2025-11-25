package com.tes.domain.groups

// import com.tes.domain.user.UserRepository (siehe Main.kt)
import java.util.Random

/**
 * Provides business logic for creating and managing groups.
 *
 * Responsibilities:
 * - Create new groups and generate invitation codes.
 * - Let users join or leave groups.
 * - Ensure only authorized users can delete groups or remove members.
 * - Provide views of a users groups and group members.
 *
 * This service works purely with domain objects and repository interfaces.
 * It does not deal with HTTP, JSON or database details.
 * The API layer calls this service and translates exceptions into HTTP responses.
 */
class GroupService(
    private val groupRepository: GroupRepository,
    // private val userRepository: UserRepository (siehe Main.kt)
) {

    /**
     * Creates a new group with a unique invitation code.
     *
     * The user who creates the group becomes both the admin and an initial member.
     *
     * @param name Group name.
     * @param adminId ID of the user who creates the group.
     * @return The created [Group] including its generated ID and invitation code.
     *
     * @throws GroupException If the name is blank.
     */
    fun createGroup(name: String, adminId: Int): Group {
        if (name.isBlank()) {
            throw GroupException("Group name must not be empty.")
        }

        // Generate a unique 6-character invitation code.
        val invitationCode = generateUniqueInvitationCode()

        // Persist the group. The repository is responsible for also adding the admin as a member.
        return groupRepository.createGroup(name, invitationCode, adminId)
    }

    /**
     * Lets a user join a group using an invitation code.
     *
     * @param invitationCode Group invitation code (must be exactly 6 characters).
     * @param userId ID of the user who wants to join.
     * @return The [Group] that the user has joined.
     *
     * @throws GroupException If the code is invalid, the group does not exist or the user is already a member.
     */
    fun joinGroup(invitationCode: String, userId: Int): Group {
        if (invitationCode.length != 6) {
            throw GroupException("Invitation code must be exactly 6 characters long.")
        }

        // Look up the group by invitation code.
        val group = groupRepository.findByInvitationCode(invitationCode)
            ?: throw GroupException("Invalid invitation code.")

        // Prevent adding the same user to the same group multiple times.
        if (groupRepository.isMember(group.id, userId)) {
            throw GroupException("User is already a member of this group.")
        }

        // Add the user as a member of the group.
        groupRepository.addMember(group.id, userId)

        return group
    }

    /**
     * Lets a user leave a group.
     *
     * The group creator (admin) is not allowed to leave the group.
     * Admins must delete the group instead so that no group is left without a responsible admin.
     *
     * @param groupId ID of the group to leave.
     * @param userId ID of the user who wants to leave.
     *
     * @throws GroupException If the group does not exist, the user is not a member or the user is the group admin.
     */
    fun leaveGroup(groupId: Int, userId: Int) {
        val group = groupRepository.findById(groupId)
            ?: throw GroupException("Group does not exist.")

        // The admin is not allowed to leave their own group.
        if (group.adminId == userId) {
            throw GroupException("Group creator cannot leave the group. Please delete the group instead.")
        }

        // Make sure the user is currently a member of this group.
        if (!groupRepository.isMember(groupId, userId)) {
            throw GroupException("User is not a member of this group.")
        }

        // Remove the user from the group.
        groupRepository.removeMember(groupId, userId)
    }

    /**
     * Deletes a group.
     *
     * Only the group admin is allowed to delete the group.
     * Deleting a group also removes all memberships (via a database cascade).
     *
     * @param groupId ID of the group to delete.
     * @param userId ID of the user who requests the deletion.
     *
     * @throws GroupException If the group does not exist or the user is not the admin.
     */
    fun deleteGroup(groupId: Int, userId: Int) {
        val group = groupRepository.findById(groupId)
            ?: throw GroupException("Group does not exist.")

        // Enforce that only the admin can delete the group.
        if (group.adminId != userId) {
            throw GroupException("Only the group creator can delete the group.")
        }

        // Delete the group and all related memberships.
        groupRepository.deleteGroup(groupId)
    }

    /**
     * Removes a member from a group.
     *
     * This operation is restricted to the group admin.
     * The admin cannot remove themselves.
     * Admins must delete the group instead if they no longer want it.
     *
     * @param groupId ID of the group.
     * @param memberUserId ID of the member to be removed.
     * @param adminUserId ID of the user performing the action (must be the admin).
     *
     * @throws GroupException If the group does not exist, the acting user is not the admin,
     *                        the admin tries to remove themselves or the target user is not a member.
     */
    fun removeMember(groupId: Int, memberUserId: Int, adminUserId: Int) {
        val group = groupRepository.findById(groupId)
            ?: throw GroupException("Group does not exist.")

        // Only the admin may remove members.
        if (group.adminId != adminUserId) {
            throw GroupException("Only the group creator can remove members.")
        }

        // The admin cannot remove themselves => they should delete the group instead.
        if (memberUserId == adminUserId) {
            throw GroupException("Group creator cannot remove themselves. Please delete the group instead.")
        }

        // Ensure the target user is currently a member of this group.
        if (!groupRepository.isMember(groupId, memberUserId)) {
            throw GroupException("User is not a member of this group.")
        }

        // Remove the member from the group.
        groupRepository.removeMember(groupId, memberUserId)
    }

    /**
     * Returns all groups a user is currently a member of.
     *
     * @param userId ID of the user.
     * @return List of [Group] instances the user belongs to.
     */
    fun getUserGroups(userId: Int): List<Group> {
        return groupRepository.findGroupsByUserId(userId)
    }

    /**
     * Generates a unique 6-character alphanumeric invitation code.
     *
     * The method repeatedly generates random codes and checks in the repository whether they are already in use.
     * It stops either when a free code is found or when a maximum number of attempts is exceeded.
     *
     * @return A unique invitation code that is not yet used by any group.
     *
     * @throws GroupException If a unique code cannot be generated after several attempts.
     */
    private fun generateUniqueInvitationCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        var code: String
        var attempts = 0

        do {
            // Build a random 6-character string from the allowed characters.
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
 * Generic exception type for group-related errors.
 *
 * This exception is thrown whenever a group-related business rule is violated.
 */
class GroupException(message: String) : Exception(message)
