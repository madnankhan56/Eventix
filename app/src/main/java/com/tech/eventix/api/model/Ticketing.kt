package com.tech.eventix.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Ticketing(
    val safeTix: SafeTix?,
    val allInclusivePricing: AllInclusivePricing?,
    val id: String? = null
)

@Serializable
data class SafeTix(
    val enabled: Boolean? = null
)

@Serializable
data class AllInclusivePricing(
    val enabled: Boolean? = null
)