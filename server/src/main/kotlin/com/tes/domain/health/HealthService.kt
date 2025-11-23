package com.tes.domain.health

/**
 * Service responsible for providing health information about the server.
 */
class HealthService {

    /**
     * Builds the current health status of the server.
     * @param uptime The server uptime in seconds since the process was started.
     * @return A [Health] instance describing the current health state.
     */
    fun getHealth(uptime: Long): Health {
        return Health(
            status = "ok",
            message = "Server is alive",
            uptimeSeconds = uptime
        )
    }
}
