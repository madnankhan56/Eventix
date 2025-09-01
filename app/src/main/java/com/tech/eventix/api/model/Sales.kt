package com.tech.eventix.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Sales(
    val `public`: PublicSale,
    val presales: List<Presale>? = null
)

@Serializable
data class PublicSale(
    val startDateTime: String,
    val startTBD: Boolean,
    val startTBA: Boolean,
    val endDateTime: String
)

@Serializable
data class Presale(
    val startDateTime: String,
    val endDateTime: String,
    val name: String
)