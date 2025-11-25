package com.tes.domain.health

import kotlinx.serialization.Serializable

/**
 * Data transfer object (DTO) representing the health status of the server.
 *
 * Instances of this class are serialized to JSON and returned by the "/health" endpoint
 * so that clients can easily check whether the server is up and how long it has been running.
 *
 * @property status Short status string ("ok").
 * @property message Description of the current status.
 * @property uptimeSeconds Number of seconds since the server process was started.
 */
@Serializable
data class Health(
    val status: String,
    val message: String,
    val uptimeSeconds: Long
)
