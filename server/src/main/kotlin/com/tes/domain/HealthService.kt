package com.tes.domain

/**
 * Service layer responsible for managing health-related logic.
 */
class HealthService {

    /**
     * Returns the health status of the server.
     * @param uptime the server uptime in seconds
     * @return the current health status
     */
    fun getHealth(uptime: Long): Health {
        return Health(
            status = "ok",
            message = "Server is alive",
            uptimeSeconds = uptime
        )
    }
}