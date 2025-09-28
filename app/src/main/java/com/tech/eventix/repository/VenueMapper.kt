package com.tech.eventix.repository

import com.tech.eventix.api.model.NetworkVenue
import com.tech.eventix.domain.Venue

/**
 * Maps a [NetworkVenue] from the API layer to a [Venue] in the domain layer.
 */
fun NetworkVenue.toDomainVenue(): Venue {
    return Venue(
        name = name ?: "",
        city = city?.name ?: "",
        state = state?.name ?: "",
        address = address?.line1 ?: "",
        parkingDetail = parkingDetail,
        generalRule = generalInfo?.generalRule,
        childRule = generalInfo?.childRule
    )
}