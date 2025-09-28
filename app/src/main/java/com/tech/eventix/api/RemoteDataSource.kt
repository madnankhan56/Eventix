package com.tech.eventix.api

import com.indiedev.networking.annotations.Cache
import com.indiedev.networking.annotations.MockResponse
import com.tech.eventix.api.model.Root
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.tech.eventix.api.model.NetworkEvent
import java.util.concurrent.TimeUnit

interface RemoteDataSource {

    @Cache(duration = 5, timeUnit = TimeUnit.MINUTES)
//    @MockResponse(resourceName = "apisample")
    @GET("discovery/v2/events.json")
    suspend fun getEvents(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("countryCode") countryCode: String = "US",
        @Query("apikey") apiKey: String,
        @Query("sort") sortBy: String = "date,asc",
        @Query("startDateTime") startDateTime: String? = null,
        @Query("keyword") keyword: String? = null
    ): Root


    @GET("discovery/v2/events/{id}")
    suspend fun getEventDetails(
        @Path("id") eventId: String,
        @Query("apikey") apiKey: String
    ): NetworkEvent
} 