package com.tech.eventix.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Seatmap(
    val staticUrl: String,
    val id: String? = null
)