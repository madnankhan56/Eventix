package com.tech.eventix.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkEvent(
    val name: String,
    val type: String? = null,
    val id: String,
    val test: Boolean? = null,
    val url: String? = null,
    val locale: String? = null,
    val images: List<Image> = emptyList(),
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
    val type: String? = null,
    val currency: String? = null,
    val min: Double? = null,
    val max: Double? = null
)

@Serializable
data class Public(
    val startDateTime: String? = null,
    val endDateTime: String? = null
)