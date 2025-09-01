package com.tech.eventix.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Image(
    val ratio: String? = null,
    val url: String,
    val width: Int,
    val height: Int,
    val fallback: Boolean,
    val attribution: String? = null
)