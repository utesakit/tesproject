package com.tes.api.groups

import kotlinx.serialization.Serializable

/**
 * Request body for creating a new group.
 */
@Serializable
data class CreateGroupRequest(
    val name: String  // Group name
)

/**
 * Request body for joining a group.
 */
@Serializable
data class JoinGroupRequest(
    val invitationCode: String  // 6-character invitation code
)

/**
 * API response for a single group.
 */
@Serializable
data class GroupResponse(
    val id: Int,                // Group database ID
    val name: String,           // Group name
    val invitationCode: String, // Invitation code
    val adminId: Int            // ID of the group admin
)

/**
 * API response for a list of groups.
 */
@Serializable
data class GroupsResponse(
    val groups: List<GroupResponse>  // All groups of the user
)
