package com.tech.eventix.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory




object NetworkModule {
    private const val BASE_URL = "https://app.ticketmaster.com/" // Replace with actual base URL

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val eventApiService: EventApiService = retrofit.create(EventApiService::class.java)
} 