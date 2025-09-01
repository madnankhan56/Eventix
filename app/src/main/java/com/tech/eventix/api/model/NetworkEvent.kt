package com.tech.eventix.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkEvent(
    val name: String,
    val type: String,
    val id: String,
    val test: Boolean,
    val url: String,
    val locale: String,
    val images: List<Image>,
    val dates: Dates,
    val classifications: List<Classification>? = null,
    @SerialName("_embedded") val embedded: EventEmbedded,
    val sales: Sales? = null,
    val priceRanges: List<Price>? = null,
    val products: List<Product>? = null,
    val info: String? = null,
    val pleaseNote: String? = null,
    val promoter: Promoter? = null,
    val seatmap: Seatmap? = null,
    val accessibility: Accessibility? = null,
    val ageRestrictions: AgeRestrictions? = null,
    val ticketLimit: TicketLimit? = null
)


@Serializable
data class Price(
    val type: String,
    val currency: String,
    val min: Double,
    val max: Double
)

@Serializable
data class Public(
    val startDateTime: String?,
    val endDateTime: String?
)