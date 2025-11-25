package com.tes.api.groups

import com.tes.api.auth.MessageResponse
import com.tes.api.auth.requireAuthenticatedUserId
import com.tes.domain.groups.GroupException
import com.tes.domain.groups.GroupService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post

/**
 * Defines all HTTP endpoints related to groups.
 *
 * Main operations:
 * - Create a new group for the authenticated user.
 * - List all groups a user is a member of.
 * - Join a group using an invitation code.
 * - Leave or delete a group.
 * - Remove members from a group.
 *
 * This layer:
 * - Uses [requireAuthenticatedUserId] to ensure the user is logged in.
 * - Delegates business decisions to [GroupService].
 * - Translates [GroupException] and generic errors into meaningful HTTP responses.
 */

/**
 * Registers all group-related routes in the Ktor routing tree.
 *
 * Endpoints:
 * - POST   /groups                                 → create a new group
 * - POST   /groups/join                            → join a group via invitation code
 * - GET    /groups                                 → list all groups of the current user
 * - DELETE /groups/{groupId}                       → delete a group (admin only)
 * - POST   /groups/{groupId}/leave                 → leave a group
 * - DELETE /groups/{groupId}/members/{memberId}    → remove a member (admin only)
 *
 * @param groupService Business logic for creating and managing groups.
 * @param jwtSecret Secret key used to validate JWT access tokens.
 * @param jwtIssuer Expected issuer value in JWT tokens.
 */
fun Route.groupRoutes(
    groupService: GroupService,
    jwtSecret: String,
    jwtIssuer: String
) {

    /**
     * POST /groups
     * Creates a new group. The authenticated user becomes admin and member.
     */
    post<CreateGroupRequest>("/groups") { request ->
        try {
            // Ensure the user is authenticated and get their user ID from the token.
            val userId = requireAuthenticatedUserId(call, jwtSecret, jwtIssuer)

            // Ask the domain service to create the group.
            val group = groupService.createGroup(request.name, userId)

            // Return the created group as JSON with HTTP 201 Created.
            call.respond(
                HttpStatusCode.Created,
                GroupResponse(
                    id = group.id,
                    name = group.name,
                    invitationCode = group.invitationCode,
                    adminId = group.adminId
                )
            )
        } catch (e: GroupException) {
            // Business rule violation.
            call.respond(
                HttpStatusCode.BadRequest,
                MessageResponse(e.message ?: "Error while creating group.")
            )
        } catch (e: Exception) {
            // If requireAuthenticatedUserId already responded with 401, just stop.
            if (e.message == "Unauthorized") {
                return@post
            }
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                MessageResponse("Internal server error while creating group.")
            )
        }
    }

    /**
     * POST /groups/join
     * Lets the authenticated user join a group via invitation code.
     */
    post<JoinGroupRequest>("/groups/join") { request ->
        try {
            val userId = requireAuthenticatedUserId(call, jwtSecret, jwtIssuer)

            val group = groupService.joinGroup(request.invitationCode, userId)

            call.respond(
                HttpStatusCode.OK,
                GroupResponse(
                    id = group.id,
                    name = group.name,
                    invitationCode = group.invitationCode,
                    adminId = group.adminId
                )
            )
        } catch (e: GroupException) {
            call.respond(
                HttpStatusCode.BadRequest,
                MessageResponse(e.message ?: "Error while joining group.")
            )
        } catch (e: Exception) {
            if (e.message == "Unauthorized") {
                return@post
            }
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                MessageResponse("Internal server error while joining group.")
            )
        }
    }

    /**
     * GET /groups
     * Returns all groups where the authenticated user is a member.
     */
    get("/groups") {
        try {
            val userId = requireAuthenticatedUserId(call, jwtSecret, jwtIssuer)

            val groups = groupService.getUserGroups(userId)

            call.respond(
                HttpStatusCode.OK,
                GroupsResponse(
                    groups = groups.map { group ->
                        GroupResponse(
                            id = group.id,
                            name = group.name,
                            invitationCode = group.invitationCode,
                            adminId = group.adminId
                        )
                    }
                )
            )
        } catch (e: Exception) {
            if (e.message == "Unauthorized") {
                return@get
            }
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                MessageResponse("Internal server error while fetching groups.")
            )
        }
    }

    /**
     * DELETE /groups/{groupId}
     * Deletes a group. Only the group admin may delete it.
     */
    delete("/groups/{groupId}") {
        try {
            val userId = requireAuthenticatedUserId(call, jwtSecret, jwtIssuer)

            // Parse and validate the path parameter "groupId".
            val groupId = call.parameters["groupId"]?.toIntOrNull()
                ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        MessageResponse("Invalid group ID.")
                    )
                    return@delete
                }

            // Ask the domain service to delete the group.
            groupService.deleteGroup(groupId, userId)

            call.respond(
                HttpStatusCode.OK,
                MessageResponse("Group deleted successfully.")
            )
        } catch (e: GroupException) {
            call.respond(
                HttpStatusCode.BadRequest,
                MessageResponse(e.message ?: "Error while deleting group.")
            )
        } catch (e: Exception) {
            if (e.message == "Unauthorized") {
                return@delete
            }
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                MessageResponse("Internal server error while deleting group.")
            )
        }
    }

    /**
     * POST /groups/{groupId}/leave
     * Lets the authenticated user leave a group.
     * TODO: "Cannot infer type for type parameter R" is only an IDEA-Error
     */
    post("/groups/{groupId}/leave") {
    try {
            val userId = requireAuthenticatedUserId(call, jwtSecret, jwtIssuer)

            val groupId = call.parameters["groupId"]?.toIntOrNull()
                ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        MessageResponse("Invalid group ID.")
                    )
                    return@post
                }

            groupService.leaveGroup(groupId, userId)

            call.respond(
                HttpStatusCode.OK,
                MessageResponse("Left group successfully.")
            )
        } catch (e: GroupException) {
            call.respond(
                HttpStatusCode.BadRequest,
                MessageResponse(e.message ?: "Error while leaving group.")
            )
        } catch (e: Exception) {
            if (e.message == "Unauthorized") {
                return@post
            }
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                MessageResponse("Internal server error while leaving group.")
            )
        }
    }

    /**
     * DELETE /groups/{groupId}/members/{memberId}
     * Removes a member from a group. Only the admin may remove members.
     */
    delete("/groups/{groupId}/members/{memberId}") {
        try {
            val adminUserId = requireAuthenticatedUserId(call, jwtSecret, jwtIssuer)

            val groupId = call.parameters["groupId"]?.toIntOrNull()
                ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        MessageResponse("Invalid group ID.")
                    )
                    return@delete
                }

            val memberUserId = call.parameters["memberId"]?.toIntOrNull()
                ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        MessageResponse("Invalid member ID.")
                    )
                    return@delete
                }

            // Ask the domain service to remove the member (admin-only action).
            groupService.removeMember(groupId, memberUserId, adminUserId)

            call.respond(
                HttpStatusCode.OK,
                MessageResponse("Member removed from group successfully.")
            )
        } catch (e: GroupException) {
            call.respond(
                HttpStatusCode.BadRequest,
                MessageResponse(e.message ?: "Error while removing member.")
            )
        } catch (e: Exception) {
            if (e.message == "Unauthorized") {
                return@delete
            }
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                MessageResponse("Internal server error while removing member.")
            )
        }
    }
}
