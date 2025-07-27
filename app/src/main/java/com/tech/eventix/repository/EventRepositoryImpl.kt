package com.tech.eventix.repository

import com.tech.eventix.api.RemoteDataSource
import com.tech.eventix.domain.Event
import com.tech.eventix.utils.ResultState
import com.tech.eventix.utils.asResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import android.content.Context
import com.google.gson.Gson
import com.tech.eventix.api.model.Root
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EventRepositoryImpl @Inject constructor(
    private val apiService: RemoteDataSource,
    private val apiKeyProvider: ApiKeyProvider,
    private val context: Context
) : EventRepository {
    
    companion object {
        private const val USE_LOCAL_JSON = false // Set to true to use local JSON, false for real API
    }
    
    override fun getEvents(page: Int, size: Int): Flow<ResultState<List<Event>>> = flow {
        if (USE_LOCAL_JSON) {
            getEventsFromLocalJson()
        } else {
            getEventsFromApi(page, size)
        }
    }.asResultState()
    
    private suspend fun FlowCollector<List<Event>>.getEventsFromLocalJson() {
        // Simulate network delay
        delay(500)
        
        try {
            // Read from local JSON file
            val jsonString = context.resources.openRawResource(
                context.resources.getIdentifier("response", "raw", context.packageName)
            ).bufferedReader().use { it.readText() }
            
            val gson = Gson()
            val root = gson.fromJson(jsonString, Root::class.java)
            val events = root._embedded.events
            
            // Return the same events for every page (no pagination)
            val domainEvents = events.map { it.toDomain() }
            emit(domainEvents)
        } catch (e: Exception) {
            throw e
        }
    }
    
    private suspend fun FlowCollector<List<Event>>.getEventsFromApi(page: Int, size: Int) {
        try {
            val apiKey = apiKeyProvider.getApiKey()
            val startDateTime = getTomorrowStartDateTime()
            val result = apiService.getEvents(
                page, size, apiKey = apiKey, sortBy = "date,asc", startDateTime = startDateTime
            )
            val events = result.getEvents()
            val domainEvents = events.map { it.toDomain() }
            emit(domainEvents)
        } catch (e: Exception) {
            throw e
        }
    }

    private fun getTomorrowStartDateTime(): String {
        val tomorrow = LocalDate.now().plusDays(1)
        return tomorrow.format(DateTimeFormatter.ISO_DATE) + "T00:00:00Z"
    }
} 