package com.tes.domain

import com.tes.data.HealthRepository

/**
 * Service layer responsible for managing health-related logic.
 */
class HealthService(
    private val repository: HealthRepository
) {

    /**
     * Returns the health status.
     * @param uptime the server uptime in seconds
     */
    fun getHealth(uptime: Long): HealthStatus {
        return repository.getHealth(uptime)
    }
} //t