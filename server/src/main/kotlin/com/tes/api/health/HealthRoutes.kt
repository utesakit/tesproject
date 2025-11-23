package com.tes.api.health

import com.tes.domain.health.HealthService
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import java.time.Duration
import java.time.Instant

/**
 * Registers the HTTP routes that expose server health information.
 * @param healthService Service used to build the health response.
 * @param serverStartTime Timestamp when the server process was started.
 */
fun Route.healthRoutes(
    healthService: HealthService,
    serverStartTime: Instant
) {

    get("/health") {

        // Calculate the server uptime in seconds based on the start time.
        val uptimeSeconds = Duration.between(serverStartTime, Instant.now()).seconds

        // Ask the domain service to create a health object for the current state
        val health = healthService.getHealth(uptimeSeconds)

        // Ktor and kotlinx.serialization will automatically render this as JSON.
        call.respond(health)
    }
}