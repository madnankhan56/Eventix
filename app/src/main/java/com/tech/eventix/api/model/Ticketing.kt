package com.tech.eventix.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Ticketing(
    val safeTix: SafeTix?,
    val allInclusivePricing: AllInclusivePricing?,
    val id: String
)

@Serializable
data class SafeTix(
    val enabled: Boolean
)

@Serializable
data class AllInclusivePricing(
    val enabled: Boolean
)