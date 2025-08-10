package com.tech.eventix.domain

/**
 * Represents a venue where an event takes place. This is part of the domain layer.
 */
data class Venue(
    val name: String,
    val city: String,
    val state: String,
    val address: String,
    val parkingDetail: String?= null,
    val generalRule: String?= null,
    val childRule: String? = null
)