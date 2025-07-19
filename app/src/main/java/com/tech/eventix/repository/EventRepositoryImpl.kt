package com.tech.eventix.repository

import com.tech.eventix.api.EventApiService
import com.tech.eventix.domain.EventDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventRepositoryImpl(private val apiService: EventApiService) : EventRepository {
    override suspend fun getEvents(page: Int?, size: Int?): List<EventDomain> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getEvents(page, size)
            if (response.isSuccessful) {
                val events = response.body()?._embedded?.events ?: emptyList()
                events.map { event ->
                    EventDomain(
                        name = event.name,
                        imageUrl = event.images.firstOrNull()?.url ?: "",
                        date = event.dates.start?.localDate ?: "",
                        time = event.dates.start?.localTime ?: "",
                        location = event._embedded?.venues?.firstOrNull()?.let { venue ->
                            listOfNotNull(venue.name, venue.city?.name, venue.state?.stateCode).joinToString(", ")
                        } ?: "",
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
} 