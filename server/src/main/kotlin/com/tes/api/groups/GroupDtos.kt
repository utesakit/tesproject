package com.tes.api.groups

import kotlinx.serialization.Serializable

/**
 * Data Transfer Objects (DTOs) used by the group-related REST API.
 *
 * These classes define:
 * - Request bodies for creating and joining groups.
 * - Response objects representing single groups and lists of groups.
 *
 * They act as a stable contract between the server and clients and hide internal database or domain model details.
 */

/**
 * Request body for creating a new group.
 *
 * Sent by the client when the current user wants to create a new group.
 *
 * @property name Name of the new group.
 */
@Serializable
data class CreateGroupRequest(
    val name: String
)

/**
 * Request body for joining an existing group.
 *
 * The client sends the invitation code that was generated when the group was created.
 * If the code is valid, the server will add the user to the group.
 *
 * @property invitationCode 6-character invitation code used to join the group.
 */
@Serializable
data class JoinGroupRequest(
    val invitationCode: String
)

/**
 * API response representing a single group.
 *
 * This is what the server sends back to the client when a group is created or when group information is requested.
 *
 * @property id Unique identifier of the group.
 * @property name Group name.
 * @property invitationCode Code that other users can use to join this group.
 * @property adminId ID of the user who administrates the group (group creator).
 */
@Serializable
data class GroupResponse(
    val id: Int,
    val name: String,
    val invitationCode: String,
    val adminId: Int
)

/**
 * API response representing a list of groups.
 *
 * Used for endpoints that return all groups of the current user.
 *
 * @property groups List of group objects, one entry per group.
 */
@Serializable
data class GroupsResponse(
    val groups: List<GroupResponse>
)
