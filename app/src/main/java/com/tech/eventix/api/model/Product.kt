package com.tech.eventix.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val name: String,
    val id: String,
    val url: String,
    val type: String
)