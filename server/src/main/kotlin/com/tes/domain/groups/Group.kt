package com.tes.domain.groups

/**
 * Domain model for a group.
 */
data class Group(
    val id: Int,                // Database ID
    val name: String,           // Group name
    val invitationCode: String, // 6-char alphanumeric invitation code
    val adminId: Int            // ID of the group creator (admin)
)

/**
 * Domain model for a group membership.
 */
data class GroupMember(
    val id: Int,           // Database ID
    val groupId: Int,      // Group ID
    val userId: Int        // User ID
)

