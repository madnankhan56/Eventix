package com.tech.eventix.api.model

data class Ticketing(
    val safeTix: SafeTix?,
    val allInclusivePricing: AllInclusivePricing?,
    val id: String
)

data class SafeTix(
    val enabled: Boolean
)

data class AllInclusivePricing(
    val enabled: Boolean
)