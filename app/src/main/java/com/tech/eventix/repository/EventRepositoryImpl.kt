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

class EventRepositoryImpl @Inject constructor(
    private val apiService: RemoteDataSource,
    private val apiKeyProvider: ApiKeyProvider,
    private val context: Context
) : EventRepository {
    
    companion object {
        private const val USE_LOCAL_JSON = false
    }
    
    override fun getEvents(page: Int, size: Int): Flow<ResultState<List<Event>>> = flow {
        if (USE_LOCAL_JSON) {
            getEventsFromLocalJson()
        } else {
            getEventsFromApi(page, size)
        }
    }.asResultState()
    
    private suspend fun FlowCollector<List<Event>>.getEventsFromLocalJson() {
        delay(500)
        
        try {
            val jsonString = context.resources.openRawResource(
                context.resources.getIdentifier("response", "raw", context.packageName)
            ).bufferedReader().use { it.readText() }
            
            val gson = Gson()
            val root = gson.fromJson(jsonString, Root::class.java)
            val events = root._embedded.events
            
            val domainEvents = events.map { it.toDomain() }
            emit(domainEvents)
        } catch (e: Exception) {
            throw e
        }
    }
    
        private suspend fun FlowCollector<List<Event>>.getEventsFromApi(page: Int, size: Int) {
        try {
            val apiKey = apiKeyProvider.getApiKey()
            val result = apiService.getEvents(page, size, apiKey = apiKey)
            val events = result.getEvents()
            val domainEvents = events.map { it.toDomain() }
            emit(domainEvents)
        } catch (e: Exception) {
            throw e
        }
    }
} 