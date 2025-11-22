package com.tes.data

import com.tes.domain.HealthStatus

/**
 * Repository abstraction for retrieving health-related information.
 */
interface HealthRepository {

    /**
     * Returns the current health status of the server.
     * @param uptime uptime in seconds since the server started
     */
    fun getHealth(uptime: Long): HealthStatus
}

/**
 * Simple in-memory implementation of [HealthRepository].
 */
class InMemoryHealthRepository : HealthRepository {

    override fun getHealth(uptime: Long): HealthStatus {
        return HealthStatus(
            status = "ok",
            message = "Server is alive",
            uptimeSeconds = uptime
        )
    }
}