package com.tech.eventix.domain

/**
 * Represents the detailed information for a single event, tailored for the event details screen.
 * This model contains more comprehensive information than the simple [Event] model used for lists.
 */
data class EventDetail(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val date: String,
    val time: String,
    val venue: Venue?,
    val info: String?,
    val seatmapUrl: String?,
    val price: String?,
    val products: List<String>,
    val genre: String?,
    val ticketLimit: String?,
    val ageRestrictions: String?
)