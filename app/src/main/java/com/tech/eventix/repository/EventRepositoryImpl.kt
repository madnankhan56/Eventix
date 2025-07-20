package com.tech.eventix.repository

import com.tech.eventix.BuildConfig
import com.tech.eventix.api.RemoteDataSource
import com.tech.eventix.domain.Event
import com.tech.eventix.utils.ResultState
import com.tech.eventix.utils.asResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val apiService: RemoteDataSource
) : EventRepository {
    override fun getEvents(page: Int?, size: Int?): Flow<ResultState<List<Event>>> = flow {
        val apiKey = BuildConfig.API_KEY
        val result = apiService.getEvents(0, 50, apiKey = apiKey)
        val events = result._embedded.events
        val domainEvents = events.map { it.toDomain() }
        emit(domainEvents)
    }.asResultState()
} 