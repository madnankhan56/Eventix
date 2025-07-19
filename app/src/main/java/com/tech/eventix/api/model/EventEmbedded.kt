package com.tech.eventix.api.model

data class EventEmbedded(
    val venues: List<NetworkVenue>,
    val attractions: List<Attraction>
)

data class NetworkVenue(
    val name: String,
    val type: String,
    val id: String,
    val test: Boolean,
    val url: String,
    val locale: String,
    val images: List<Image>?,
    val postalCode: String?,
    val timezone: String?,
    val city: City,
    val state: State,
    val country: Country?,
    val address: Address,
    val location: Location?,
    val markets: List<Market>?,
    val dmas: List<Dma>?,
    val boxOfficeInfo: BoxOfficeInfo?,
    val parkingDetail: String?,
    val upcomingEvents: UpcomingEvents?,
    val ada: Ada?,
    val classifications: List<Classification>?,
    val _links: VenueLinks?
)

data class City(
    val name: String
)

data class State(
    val name: String,
    val stateCode: String
)

data class Country(
    val name: String,
    val countryCode: String
)

data class Address(
    val line1: String
)

data class Location(
    val longitude: String,
    val latitude: String
)

data class Market(
    val name: String,
    val id: String
)

data class Dma(
    val id: Int
)

data class BoxOfficeInfo(
    val openHoursDetail: String?
)

data class UpcomingEvents(
    val archtics: Int? = null,
    val tmr: Int? = null,
    val ticketmaster: Int? = null,
    val _total: Int,
    val _filtered: Int
)

data class Ada(
    val adaPhones: String?,
    val adaCustomCopy: String?,
    val adaHours: String?
)

data class VenueLinks(
    val self: Link
)

data class Attraction(
    val name: String,
    val type: String,
    val id: String,
    val test: Boolean,
    val url: String,
    val locale: String,
    val externalLinks: ExternalLinks?,
    val images: List<Image>?,
    val aliases: List<String>?,
    val classifications: List<Classification>?,
    val upcomingEvents: UpcomingEvents?,
    val _links: VenueLinks?
)

data class ExternalLinks(
    val twitter: List<ExternalUrl>?,
    val facebook: List<ExternalUrl>?,
    val wiki: List<ExternalUrl>?,
    val instagram: List<ExternalUrl>?,
    val homepage: List<ExternalUrl>?
)

data class ExternalUrl(
    val url: String
)

