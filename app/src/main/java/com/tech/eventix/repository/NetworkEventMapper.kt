package com.tech.eventix.repository

import com.tech.eventix.api.model.NetworkEvent
import com.tech.eventix.domain.Event

fun NetworkEvent.toDomain(): Event {
    return Event(
        name = this.name,
        imageUrl = this.images.firstOrNull()?.url.orEmpty(),
        date = this.dates.start.localDate,
        time = this.dates.start.localTime ?: "19:00:00",
        venue = this._embedded.venues.firstOrNull()?.let { venue ->
            com.tech.eventix.domain.Venue(
                name = venue.name,
                city = venue.city.name ,
                state = venue.state.stateCode,
                address = venue.address.line1
            )
        },
        test = this.test
    )
} 