package com.tech.eventix.api.model

import kotlinx.serialization.Serializable

@Serializable
data class EventEmbedded(
    val venues: List<NetworkVenue> = emptyList(),
    val attractions: List<Attraction> = emptyList()
)

@Serializable
data class NetworkVenue(
    val name: String? = null,
    val type: String? = null,
    val id: String? = null,
    val test: Boolean? = null,
    val url: String? = null,
    val locale: String? = null,
    val images: List<Image>? = emptyList(),
    val postalCode: String? = null,
    val timezone: String? = null,
    val city: City? = null,
    val state: State? = null,
    val country: Country? = null,
    val address: Address? = null,
    val location: Location? = null,
    val markets: List<Market>? = null,
    val dmas: List<Dma>? = null,
    val boxOfficeInfo: BoxOfficeInfo? = null,
    val parkingDetail: String? = null,
    val generalInfo: GeneralInfo? = null,
    val upcomingEvents: UpcomingEvents? = null,
    val ada: Ada? = null,
    val classifications: List<Classification>? = null,
    val _links: VenueLinks? = null
)

@Serializable
data class GeneralInfo(
    val generalRule: String? = null,
    val childRule: String? = null
)

@Serializable
data class City(
    val name: String? = null
)

@Serializable
data class State(
    val name: String? = null,
    val stateCode: String? = null
)

@Serializable
data class Country(
    val name: String? = null,
    val countryCode: String? = null
)

@Serializable
data class Address(
    val line1: String? = null
)

@Serializable
data class Location(
    val longitude: String? = null,
    val latitude: String? = null
)

@Serializable
data class Market(
    val name: String? = null,
    val id: String? = null
)

@Serializable
data class Dma(
    val id: Int? = null
)

@Serializable
data class BoxOfficeInfo(
    val openHoursDetail: String? = null
)

@Serializable
data class UpcomingEvents(
    val archtics: Int? = null,
    val tmr: Int? = null,
    val ticketmaster: Int? = null,
    val _total: Int? = null,
    val _filtered: Int? = null
)

@Serializable
data class Ada(
    val adaPhones: String? = null,
    val adaCustomCopy: String? = null,
    val adaHours: String? = null
)

@Serializable
data class VenueLinks(
    val self: Link? = null
)

@Serializable
data class Attraction(
    val name: String? = null,
    val type: String? = null,
    val id: String? = null,
    val test: Boolean? = null,
    val url: String? = null,
    val locale: String? = null,
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

