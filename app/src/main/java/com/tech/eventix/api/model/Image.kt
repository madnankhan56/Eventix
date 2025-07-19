package com.tech.eventix.api.model

data class Image(
    val ratio: String,
    val url: String,
    val width: Int,
    val height: Int,
    val fallback: Boolean,
    val attribution: String? = null
)