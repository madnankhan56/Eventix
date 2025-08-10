package com.tech.eventix.usecase

import com.tech.eventix.domain.EventDetail
import com.tech.eventix.repository.EventRepository
import com.tech.eventix.uistate.EventDetailUiState
import com.tech.eventix.utils.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class GetEventDetailsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    operator fun invoke(eventId: String): Flow<ResultState<EventDetailUiState>> {
        return eventRepository.getEventDetails(eventId).map { result ->
            when (result) {
                is ResultState.Success -> ResultState.Success(result.data.toUiState())
                is ResultState.Error -> result
            }
        }
    }

    private fun EventDetail.toUiState(): EventDetailUiState {
        val formattedDate = formatDate(date)
        val formattedTime = formatTime(time)
        val dateTimeCombined = listOf(formattedDate, formattedTime).filter { it.isNotEmpty() }.joinToString(", ")

        val location = listOfNotNull(venue?.name, venue?.city)
            .filter { it.isNotEmpty() }
            .joinToString(", ")

        return EventDetailUiState(
            name = name,
            image = imageUrl.orEmpty(),
            dateTime = dateTimeCombined,
            location = location,
            price = price,
            info = info,
            seatmapUrl = seatmapUrl,
            products = products,
            genre = genre,
            ticketLimit = ticketLimit,
            ageRestrictions = ageRestrictions
        )
    }

    private fun formatDate(date: String): String {
        return try {
            LocalDate.parse(date).format(DateTimeFormatter.ofPattern("EEE, d MMMM", Locale.ENGLISH))
        } catch (e: Exception) {
            date
        }
    }

    private fun formatTime(time: String): String {
        return try {
            LocalTime.parse(time).format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)).lowercase()
        } catch (e: Exception) {
            time
        }
    }
}