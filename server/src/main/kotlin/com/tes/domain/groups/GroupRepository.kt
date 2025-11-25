package com.tes.domain.groups

/**
 * Abstraction for persistence operations related to groups and group memberships.
 *
 * This interface lives in the domain layer and describes what the application
 * needs from group storage, without saying anything about how it is implemented.
 *
 * Implementations are responsible for:
 * - Creating and deleting groups.
 * - Adding and removing group members.
 * - Loading all groups for a given user.
 * - Loading all members for a given group.
 *
 * The [GroupService] uses this interface to perform its business logic.
 */
interface GroupRepository {

    /**
     * Creates a new group in the underlying data store.
     *
     * @param name Group name.
     * @param invitationCode 6-character invitation code used to join the group.
     * @param adminId ID of the user who will be the group creator/admin.
     * @return The created [Group] with its generated database ID.
     */
    fun createGroup(name: String, invitationCode: String, adminId: Int): Group

    /**
     * Finds a group by its unique ID.
     *
     * @param id Group ID to search for.
     * @return The matching [Group] or "null" if no group exists with this ID.
     */
    fun findById(id: Int): Group?

    /**
     * Finds a group by its invitation code.
     *
     * @param invitationCode Code users can enter to join the group.
     * @return The matching [Group] or "null" if no group exists with this code.
     */
    fun findByInvitationCode(invitationCode: String): Group?

    /**
     * Deletes a group.
     *
     * The implementation may also delete all related group memberships (cascade => done!).
     *
     * @param id ID of the group to delete.
     */
    fun deleteGroup(id: Int)

    /**
     * Adds a user as a member of a group.
     *
     * @param groupId ID of the group to join.
     * @param userId ID of the user who should be added as a member.
     * @return The created [GroupMember] entry representing this membership.
     */
    fun addMember(groupId: Int, userId: Int): GroupMember

    /**
     * Removes a user from a group.
     *
     * @param groupId ID of the group.
     * @param userId ID of the user who should be removed as a member.
     */
    fun removeMember(groupId: Int, userId: Int)

    /**
     * Checks whether a user is currently a member of a group.
     *
     * @param groupId ID of the group.
     * @param userId ID of the user.
     * @return "true" if the user is a member of the group, "false" otherwise.
     */
    fun isMember(groupId: Int, userId: Int): Boolean

    /**
     * Returns all groups a user is a member of.
     *
     * @param userId ID of the user.
     * @return List of [Group]s the user belongs to.
     */
    fun findGroupsByUserId(userId: Int): List<Group>

    /**
     * Returns all members of a given group.
     *
     * @param groupId ID of the group.
     * @return List of [GroupMember] entries for all members of this group.
     */
    fun findMembersByGroupId(groupId: Int): List<GroupMember>
}
