package com.tes.domain.health

import kotlinx.serialization.Serializable

/**
 * Represents the current health of the server.
 * @property status Indicator "ok" for a functional server.
 * @property message Description providing additional context.
 * @property uptimeSeconds Total time in seconds the server process has been running.
 */
@Serializable
data class Health(
    val status: String,
    val message: String,
    val uptimeSeconds: Long
)
