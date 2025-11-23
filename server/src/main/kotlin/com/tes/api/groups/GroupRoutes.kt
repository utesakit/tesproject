package com.tes.api.groups

import com.tes.api.auth.MessageResponse
import com.tes.api.auth.requireAuthenticatedUserId
import com.tes.domain.groups.GroupException
import com.tes.domain.groups.GroupService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post

/**
 * Registers HTTP routes for group management.
 * Delegates all business logic to [GroupService].
 * @param groupService Service for group operations.
 * @param jwtSecret JWT secret used for token validation.
 * @param jwtIssuer JWT issuer used for token validation.
 */
fun Route.groupRoutes(
    groupService: GroupService,
    jwtSecret: String,
    jwtIssuer: String
) {

    /**
     * POST /groups
     * Creates a new group.
     * The authenticated user becomes admin and member.
     */
    post("/groups") {
        try {
            val userId = requireAuthenticatedUserId(call, jwtSecret, jwtIssuer)
            val request = call.receive<CreateGroupRequest>()

            val group = groupService.createGroup(request.name, userId)

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
            call.respond(
                HttpStatusCode.BadRequest,
                MessageResponse(e.message ?: "Error while creating group.")
            )
        } catch (e: Exception) {
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
    post("/groups/join") {
        try {
            val userId = requireAuthenticatedUserId(call, jwtSecret, jwtIssuer)
            val request = call.receive<JoinGroupRequest>()

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
            val groupId = call.parameters["groupId"]?.toIntOrNull()
                ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        MessageResponse("Invalid group ID.")
                    )
                    return@delete
                }

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

