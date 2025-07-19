package com.tech.eventix.repository

import com.tech.eventix.api.model.NetworkEvent
import com.tech.eventix.domain.Event
import com.tech.eventix.domain.Venue

fun NetworkEvent.toDomain(): Event {
    return Event(
        name = this.name,
        imageUrl = this.images.firstOrNull()?.url.orEmpty(),
        date = this.dates.start.localDate,
        time = this.dates.start.localTime,
        venue = this._embedded.venues.first().let { venue ->
            Venue(venue.name, venue.city.name, venue.state.name, venue.address.line1)
        }
    )
} 