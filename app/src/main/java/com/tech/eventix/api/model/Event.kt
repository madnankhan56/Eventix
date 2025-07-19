package com.tech.eventix.api.model

data class Event(
    val name: String,
    val type: String,
    val id: String,
    val test: Boolean,
    val url: String,
    val locale: String,
    val images: List<Image>,
    val sales: Sales,
    val dates: Dates,
    val classifications: List<Classification>,
    val promoter: Promoter,
    val promoters: List<Promoter>,
    val info: String?,
    val pleaseNote: String?,
    val seatmap: Seatmap?,
    val accessibility: Accessibility?,
    val ticketLimit: TicketLimit?,
    val ageRestrictions: AgeRestrictions?,
    val ticketing: Ticketing?,
    val _links: EventLinks,
    val _embedded: EventEmbedded
)