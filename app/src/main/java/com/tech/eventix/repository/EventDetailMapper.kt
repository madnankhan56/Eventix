package com.tech.eventix.repository

import com.tech.eventix.api.model.NetworkEvent
import com.tech.eventix.domain.EventDetail
import java.util.Locale

fun NetworkEvent.toDomainEventDetail(): EventDetail {
    val bestImage = images.find { it.ratio == "16_9" && it.width >= 1024 } ?: images.firstOrNull()

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
        ageRestrictions = ageRestrictions?.legalAgeEnforced?.let { if (it) "Enforced" else "Not Enforced" },
        ticketUrl = url
    )
}