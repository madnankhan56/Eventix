package com.tech.eventix.usecase

import com.tech.eventix.domain.Event
import com.tech.eventix.repository.EventRepository
import com.tech.eventix.utils.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class GetEventsUseCase @Inject constructor(
    private val repository: EventRepository
) {
     operator fun invoke(page: Int? = null, size: Int? = null): Flow<ResultState<List<Event>>> {
        return repository.getEvents(page, size).map { result ->
            when (result) {
                is ResultState.Success -> {
                    val filteredEvents = result.data.filter { event ->
                        event.name.isNotEmpty() &&
                        !event.imageUrl.isNullOrEmpty() &&
                        event.venue != null &&
                        event.venue.name.isNotEmpty() &&
                        event.venue.city.isNotEmpty() &&
                        event.venue.state.isNotEmpty() &&
                        event.venue.address.isNotEmpty() &&
                        !event.test
                    }
                    val sortedEvents = filteredEvents.sortedWith(compareBy(
                        { LocalDate.parse(it.date) },
                        { LocalTime.parse(it.time) }
                    )).map { event ->
                        event.copy(
                            date = formatDate(event.date),
                            time = formatTime(event.time)
                        )
                    }
                    ResultState.Success(sortedEvents)
                }
                is ResultState.Error -> result
                is ResultState.Loading -> result
            }
        }
    }

    private fun formatDate(date: String): String {
        return try {
            LocalDate.parse(date)
                .format(DateTimeFormatter.ofPattern("EEE, d MMMM", Locale.ENGLISH))
        } catch (e: Exception) {
            date
        }
    }

    private fun formatTime(time: String): String {
        return try {
            LocalTime.parse(time)
                .format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)).lowercase()
        } catch (e: Exception) {
            time
        }
    }
} 