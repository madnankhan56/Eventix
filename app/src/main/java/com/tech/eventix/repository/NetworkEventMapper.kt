package com.tech.eventix.repository

import com.tech.eventix.api.model.NetworkEvent
import com.tech.eventix.domain.Event

fun NetworkEvent.toDomain(): Event {
    return Event(
        name = this.name,
        imageUrl = this.images.firstOrNull()?.url.orEmpty(),
        date = this.dates.start.localDate,
        time = this.dates.start.localTime,
        location = this._embedded.venues.firstOrNull()?.let { venue ->
            listOfNotNull(venue.name, venue.city?.name, venue.state?.stateCode).joinToString(", ")
        } ?: ""
    )
} 