package com.tech.eventix.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Accessibility(
    val info: String? = null,
    val ticketLimit: Int? = null,
    val id: String? = null
)