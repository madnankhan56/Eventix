package com.tech.eventix.repository

import com.tech.eventix.api.model.NetworkEvent
import com.tech.eventix.domain.Event
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun NetworkEvent.toDomain(): Event {
    val formattedDate = try {
        LocalDate.parse(this.dates.start.localDate)
            .format(DateTimeFormatter.ofPattern("EEE, d MMMM", Locale.ENGLISH))
    } catch (e: Exception) {
        this.dates.start.localDate
    }
    val formattedTime = try {
        LocalTime.parse(this.dates.start.localTime)
            .format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)).lowercase()
    } catch (e: Exception) {
        this.dates.start.localTime
    }
    return Event(
        name = this.name,
        imageUrl = this.images.firstOrNull()?.url.orEmpty(),
        date = formattedDate,
        time = formattedTime,
        venue = this._embedded.venues.firstOrNull()?.let { venue ->
            com.tech.eventix.domain.Venue(
                name = venue.name,
                city = venue.city?.name ?: "",
                state = venue.state?.stateCode ?: "",
                address = venue.address?.line1 ?: ""
            )
        },
        test = this.test
    )
} 