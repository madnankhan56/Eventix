package com.tech.eventix.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Sales(
    val `public`: PublicSale? = null,
    val presales: List<Presale>? = null
)

@Serializable
data class PublicSale(
    val startDateTime: String? = null,
    val startTBD: Boolean? = null,
    val startTBA: Boolean? = null,
    val endDateTime: String? = null
)

@Serializable
data class Presale(
    val startDateTime: String? = null,
    val endDateTime: String? = null,
    val name: String? = null
)