package com.tech.eventix.api.model

data class Sales(
    val `public`: PublicSale,
    val presales: List<Presale>
)

data class PublicSale(
    val startDateTime: String,
    val startTBD: Boolean,
    val startTBA: Boolean,
    val endDateTime: String
)

data class Presale(
    val startDateTime: String,
    val endDateTime: String,
    val name: String
)