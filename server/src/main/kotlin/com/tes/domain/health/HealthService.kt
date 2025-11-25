package com.tes.domain.health

/**
 * Service that creates [Health] objects for the health check endpoint.
 *
 * This class belongs to the domain layer and contains the simple
 * business logic for representing the servers current health status.
 * It does not know anything about HTTP or Ktor, it just returns plain data.
 */
class HealthService {

    /**
     * Builds a [Health] object using the given uptime value.
     *
     * @param uptime Number of seconds the server has been running.
     * @return A [Health] instance containing status, message and uptime information for the "/health" endpoint.
     */
    fun getHealth(uptime: Long): Health {
        return Health(
            status = "ok",
            message = "Server is alive",
            uptimeSeconds = uptime
        )
    }
}
