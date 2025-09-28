package com.tech.eventix.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Image(
    val ratio: String? = null,
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val fallback: Boolean? = null,
    val attribution: String? = null
)