package com.tes.domain

import kotlinx.serialization.Serializable

/**
 * Represents the current health status of the server.
 * @param status "ok"
 * @param message additional context or diagnostic information
 * @param uptimeSeconds total runtime of the server in seconds
 */

@Serializable
data class Health(
    val status: String,
    val message: String,
    val uptimeSeconds: Long
)