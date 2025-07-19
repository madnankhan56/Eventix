package com.tech.eventix.api

import com.tech.eventix.api.model.Root
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

// Root is the top-level DTO for the API response
interface EventApiService {

    @GET("discovery/v2/events.json")
    suspend fun getEvents(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("countryCode") countryCode: String= "US",
        @Query("apikey") apiKey: String = API_KEY
    ): Response<Root>


    companion object {
        const val API_KEY = "jC1UidLnQw5p4KoSbKoSxmLWgO0USXH5"
    }

} 