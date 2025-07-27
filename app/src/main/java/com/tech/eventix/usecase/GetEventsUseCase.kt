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

    operator fun invoke(page: Int, size: Int = PAGE_SIZE): Flow<ResultState<List<Event>>> {
        return repository.getEvents(page, size).map { result ->
            when (result) {
                is ResultState.Success -> {
                    val processedEvents = result.data
                        .let(::filterValidEvents)
                        .let(::sortEventsByDateTime)
                        .let(::formatEventDatesAndTimes)
                    
                    ResultState.Success(processedEvents)
                }
                is ResultState.Error -> {
                    result
                }
            }
        }
    }


    private fun filterValidEvents(events: List<Event>): List<Event> {
        return events.filter { event ->
            isEventValid(event)
        }
    }


    private fun isEventValid(event: Event): Boolean {
        return event.name.isNotEmpty() &&
                !event.imageUrl.isNullOrEmpty() &&
                hasValidVenue(event) &&
                !event.test
    }


    private fun hasValidVenue(event: Event): Boolean {
        val venue = event.venue ?: return false
        return venue.name.isNotEmpty() &&
                venue.city.isNotEmpty() &&
                venue.state.isNotEmpty() &&
                venue.address.isNotEmpty()
    }


    private fun sortEventsByDateTime(events: List<Event>): List<Event> {
        return events.sortedWith(
            compareBy<Event> { LocalDate.parse(it.date) }
                .thenBy { LocalTime.parse(it.time) }
        )
    }


    private fun formatEventDatesAndTimes(events: List<Event>): List<Event> {
        return events.map { event ->
            event.copy(
                date = formatDate(event.date),
                time = formatTime(event.time)
            )
        }
    }


    private fun formatDate(date: String): String {
        return try {
            LocalDate.parse(date)
                .format(DateTimeFormatter.ofPattern("EEE, d MMMM", Locale.ENGLISH))
        } catch (e: Exception) {
            date // Return original if parsing fails
        }
    }


    private fun formatTime(time: String): String {
        return try {
            LocalTime.parse(time)
                .format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH))
                .lowercase()
        } catch (e: Exception) {
            time // Return original if parsing fails
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
} 