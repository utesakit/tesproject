package com.tes.domain

import kotlinx.serialization.Serializable

/**
 * Represents the current health status of the server.
 * @param status human-readable state like "OK", "WARN", or "ERROR"
 * @param message additional context or diagnostic information
 * @param uptimeSeconds total runtime of the server in seconds
 */

@Serializable
data class HealthStatus(
    val status: String,
    val message: String,
    val uptimeSeconds: Long
) //t