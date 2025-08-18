package com.tech.eventix.usecase

import com.tech.eventix.domain.EventDetail
import com.tech.eventix.repository.EventRepository
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
    operator fun invoke(eventId: String): Flow<ResultState<EventDetail>> {
        return eventRepository.getEventDetails(eventId).map { result ->
            when (result) {
                is ResultState.Success -> ResultState.Success(result.data.withFormattedFields())
                is ResultState.Error -> result
            }
        }
    }

    private fun EventDetail.withFormattedFields(): EventDetail {
        val formattedDate = formatDate(date)
        val formattedTime = formatTime(time)

        return copy(
            date = formattedDate,
            time = formattedTime
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