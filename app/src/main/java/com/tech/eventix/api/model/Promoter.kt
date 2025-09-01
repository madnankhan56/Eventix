package com.tech.eventix.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Promoter(
    val id: String,
    val name: String,
    val description: String?
)