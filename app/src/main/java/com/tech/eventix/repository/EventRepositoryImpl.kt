package com.tech.eventix.repository

import com.tech.eventix.api.RemoteDataSource
import com.tech.eventix.domain.Event
import com.tech.eventix.domain.EventDetail
import com.tech.eventix.utils.ResultState
import com.tech.eventix.utils.asResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlinx.coroutines.flow.FlowCollector
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EventRepositoryImpl @Inject constructor(
    private val apiService: RemoteDataSource,
    private val apiKeyProvider: ApiKeyProvider,
) : EventRepository {

    override fun getEvents(page: Int, size: Int, keyword: String?) = flow {
        getEventsFromApi(page, size, keyword)
    }.asResultState()

    private suspend fun FlowCollector<List<Event>>.getEventsFromApi(page: Int, size: Int, keyword: String?) {
        try {
            val apiKey = apiKeyProvider.getApiKey()
            val startDateTime = getTomorrowStartDateTime()
            val result = apiService.getEvents(
                page, size, apiKey = apiKey, sortBy = "date,asc", startDateTime = startDateTime, keyword = keyword
            )
            val events = result.getEvents()
            val domainEvents = events.map { it.toDomain() }
            emit(domainEvents)
        } catch (e: Exception) {
            throw e
        }
    }

    override fun getEventDetails(eventId: String): Flow<ResultState<EventDetail>> = flow {
        val networkEvent = apiService
            .getEventDetails(eventId, apiKeyProvider.getApiKey())

        emit(networkEvent.toDomainEventDetail())
    }.asResultState()

    private fun getTomorrowStartDateTime(): String {
        val tomorrow = LocalDate.now().plusDays(1)
        return tomorrow.format(DateTimeFormatter.ISO_DATE) + "T00:00:00Z"
    }
} 