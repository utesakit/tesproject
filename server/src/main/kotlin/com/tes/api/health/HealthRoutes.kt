package com.tes.api.health

import com.tes.domain.health.HealthService
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import java.time.Duration
import java.time.Instant

/**
 * Registers a simple health check endpoint for the server.
 *
 * This is part of the API (HTTP) layer and exposes a single route:
 *
 * - "GET /health": returns a JSON object with status, message and uptime in seconds.
 *
 * The endpoint can be used by:
 * - the Android app to verify that the backend is reachable
 *
 * @param healthService Domain service that builds the health response object.
 * @param serverStartTime Timestamp captured when the server process was started.
 */
fun Route.healthRoutes(
    healthService: HealthService,
    serverStartTime: Instant
) {

    get("/health") {
        // Calculate the current server uptime in seconds based on the start time.
        val uptimeSeconds = Duration.between(serverStartTime, Instant.now()).seconds

        // Ask the domain service to create a Health object for the current state.
        val health = healthService.getHealth(uptimeSeconds)

        // Ktor + kotlinx.serialization will automatically serialize this to JSON.
        call.respond(health)
    }
}
