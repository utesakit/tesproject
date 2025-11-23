package com.tes.api

import com.tes.domain.HealthService

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.Duration
import java.time.Instant

/**
 * Registers the health-related HTTP routes for the server.
 * @param healthService business logic for computing health information
 * @param serverStartTime timestamp when the server was started
 */
fun Route.healthRoutes(
    healthService: HealthService,
    serverStartTime: Instant
) {

    get("/health") {

        // Calculate uptime in seconds.
        val uptimeSeconds = Duration.between(serverStartTime, Instant.now()).seconds

        // Delegate actual health-state creation to the service.
        val health = healthService.getHealth(uptimeSeconds)

        // Ktor + kotlinx.serialization will automatically turn this into JSON.
        call.respond(health)
    }
}