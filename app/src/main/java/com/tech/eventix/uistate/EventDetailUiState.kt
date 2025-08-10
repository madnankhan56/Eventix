package com.tech.eventix.uistate

/**
 * Lightweight UI model for the event-details screen.
 * Keeps ONLY what the UI needs in a readily displayable format.
 */
data class EventDetailUiState(
    val name: String,
    val image: String,
    val dateTime: String,
    val location: String,
    val price: String?,
    val info: String?,
    val seatmapUrl: String?,
    val products: List<String>,
    val genre: String?,
    val ticketLimit: String?,
    val ageRestrictions: String?
)