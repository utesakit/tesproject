package com.tes.domain

/**
 * Service layer responsible for managing health-related logic.
 */
class HealthService {

    /**
     * Returns the health status.
     * @param uptime the server uptime in seconds
     */
    fun getHealth(uptime: Long): HealthStatus {
        return HealthStatus(
            status = "ok",
            message = "Server is alive",
            uptimeSeconds = uptime
        )
    }
}