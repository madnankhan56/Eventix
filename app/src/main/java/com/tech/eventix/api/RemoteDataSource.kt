package com.tech.eventix.api

import com.tech.eventix.api.model.Root
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.Response
import com.tech.eventix.api.model.NetworkEvent

interface RemoteDataSource {
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


    @Caceh(muinutes = 5)
    @GET("discovery/v2/events/{id}")
    suspend fun getEventDetails(
        @Path("id") eventId: String,
        @Query("apikey") apiKey: String
    ): NetworkEvent
} 