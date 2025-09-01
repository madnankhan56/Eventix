package com.tech.eventix.api.model

import kotlinx.serialization.Serializable

@Serializable
data class EventEmbedded(
    val venues: List<NetworkVenue> = emptyList(),
    val attractions: List<Attraction> = emptyList()
)

@Serializable
data class NetworkVenue(
    val name: String,
    val type: String,
    val id: String,
    val test: Boolean,
    val url: String,
    val locale: String,
    val images: List<Image>? = emptyList(),
    val postalCode: String?,
    val timezone: String?,
    val city: City,
    val state: State,
    val country: Country?,
    val address: Address,
    val location: Location?,
    val markets: List<Market>?,
    val dmas: List<Dma>?,
    val boxOfficeInfo: BoxOfficeInfo? = null,
    val parkingDetail: String? = null,
    val generalInfo: GeneralInfo? = null,
    val upcomingEvents: UpcomingEvents?,
    val ada: Ada? = null,
    val classifications: List<Classification>? = null,
    val _links: VenueLinks?
)

@Serializable
data class GeneralInfo(
    val generalRule: String? = null,
    val childRule: String? = null
)

@Serializable
data class City(
    val name: String
)

@Serializable
data class State(
    val name: String,
    val stateCode: String
)

@Serializable
data class Country(
    val name: String,
    val countryCode: String
)

@Serializable
data class Address(
    val line1: String
)

@Serializable
data class Location(
    val longitude: String,
    val latitude: String
)

@Serializable
data class Market(
    val name: String,
    val id: String
)

@Serializable
data class Dma(
    val id: Int
)

@Serializable
data class BoxOfficeInfo(
    val openHoursDetail: String?
)

@Serializable
data class UpcomingEvents(
    val archtics: Int? = null,
    val tmr: Int? = null,
    val ticketmaster: Int? = null,
    val _total: Int,
    val _filtered: Int
)

@Serializable
data class Ada(
    val adaPhones: String?,
    val adaCustomCopy: String?,
    val adaHours: String?
)

@Serializable
data class VenueLinks(
    val self: Link
)

@Serializable
data class Attraction(
    val name: String,
    val type: String,
    val id: String,
    val test: Boolean,
    val url: String,
    val locale: String,
    val externalLinks: ExternalLinks? = null,
    val images: List<Image>? = null,
    val aliases: List<String>? = null,
    val classifications: List<Classification>? = null,
    val upcomingEvents: UpcomingEvents? = null,
    val _links: VenueLinks? = null
)

@Serializable
data class ExternalLinks(
    val twitter: List<ExternalUrl>? = null,
    val facebook: List<ExternalUrl>? = null,
    val wiki: List<ExternalUrl>? = null,
    val instagram: List<ExternalUrl>? = null,
    val homepage: List<ExternalUrl>? = null
)

@Serializable
data class ExternalUrl(
    val url: String
)

