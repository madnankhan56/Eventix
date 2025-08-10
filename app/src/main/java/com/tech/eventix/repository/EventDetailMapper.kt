package com.tech.eventix.repository

import com.tech.eventix.api.model.NetworkEvent
import com.tech.eventix.domain.EventDetail
import java.util.Locale

/**
 * Maps a [NetworkEvent] from the API layer to an [EventDetail] in the domain layer.
 */
fun NetworkEvent.toDomainEventDetail(): EventDetail {
    // The API provides multiple images; we find the best one, prioritizing larger formats like TABLET_LANDSCAPE_LARGE_16_9.
    val bestImage = images.find { it.ratio == "16_9" && it.width >= 1024 } ?: images.firstOrNull()

    // Combine the 'info' and 'pleaseNote' fields for a comprehensive overview.
    val combinedInfo = listOfNotNull(info, pleaseNote).joinToString("\n\n")

    val priceString = priceRanges?.firstOrNull()?.let {
        String.format(Locale.US, "$%.2f - $%.2f", it.min, it.max)
    }

    val productNames = products?.map { it.name } ?: emptyList()

    return EventDetail(
        id = id,
        name = name,
        imageUrl = bestImage?.url,
        date = dates.start.localDate,
        time = dates.start.localTime ?: "",
        venue = embedded?.venues?.firstOrNull()?.toDomainVenue(),
        info = combinedInfo.takeIf { it.isNotBlank() },
        seatmapUrl = seatmap?.staticUrl,
        price = priceString,
        products = productNames,
        genre = classifications.firstOrNull { it.primary }?.genre?.name,
        ticketLimit = ticketLimit?.info,
        ageRestrictions = ageRestrictions?.legalAgeEnforced?.let { if (it) "Enforced" else "Not Enforced" }
    )
}