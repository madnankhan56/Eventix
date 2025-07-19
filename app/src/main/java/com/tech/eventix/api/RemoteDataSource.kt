package com.tech.eventix.api

import com.tech.eventix.api.model.Root
import retrofit2.http.GET
import retrofit2.http.Query

interface RemoteDataSource {
    @GET("discovery/v2/events.json")
    suspend fun getEvents(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("countryCode") countryCode: String = "US",
        @Query("apikey") apiKey: String
    ): Root
} 