package com.tes.domain.groups

/**
 * Domain model representing a group of users.
 *
 * @property id Database-generated identifier for the group.
 * @property name Group name.
 * @property invitationCode 6-character code that other users can enter to join this group.
 * @property adminId ID of the user who administrates the group (the group creator).
 */
data class Group(
    val id: Int,
    val name: String,
    val invitationCode: String,
    val adminId: Int
)

/**
 * Domain model representing a membership of a user in a group.
 *
 * Each instance links exactly one user to exactly one group.
 *
 * @property id Database-generated identifier for this membership.
 * @property groupId ID of the group the user belongs to.
 * @property userId ID of the user who is a member of the group.
 */
data class GroupMember(
    val id: Int,
    val groupId: Int,
    val userId: Int
)
