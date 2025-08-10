package com.tech.eventix.api.model

import com.google.gson.annotations.SerializedName

data class NetworkEvent(
    val name: String,
    val type: String,
    val id: String,
    val test: Boolean,
    val url: String,
    val locale: String,
    val images: List<Image>,
    val dates: Dates,
    val classifications: List<Classification>,
    @SerializedName("_embedded") val embedded: EventEmbedded?,
    val sales: Sales?,
    val priceRanges: List<Price>?,
    val products: List<Product>?,
    val info: String?,
    val pleaseNote: String?,
    val promoter: Promoter?,
    val seatmap: Seatmap?,
    val accessibility: Accessibility?,
    val ageRestrictions: AgeRestrictions?,
    val ticketLimit: TicketLimit?
)

data class Promoter(
    val name: String?,
    val description: String?
)

data class Seatmap(
    val staticUrl: String?
)

data class Sales(
    val public: Public,
    val presales: List<Presale>?
)

data class Price(
    val type: String,
    val currency: String,
    val min: Double,
    val max: Double
)

data class Public(
    val startDateTime: String?,
    val endDateTime: String?
)