package com.tech.eventix.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Dates(
    val start: StartDate? = null,
    val timezone: String? = null,
    val status: Status? = null,
    val spanMultipleDays: Boolean? = null
)

@Serializable
data class StartDate(
    val localDate: String? = null,
    val localTime: String? = null,
    val dateTime: String? = null,
    val dateTBD: Boolean? = null,
    val dateTBA: Boolean? = null,
    val timeTBA: Boolean? = null,
    val noSpecificTime: Boolean? = null
)

@Serializable
data class Status(
    val code: String? = null
)