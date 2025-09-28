package com.tech.eventix.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TicketLimit(
    val info: String? = null,
    val id: String? = null
)