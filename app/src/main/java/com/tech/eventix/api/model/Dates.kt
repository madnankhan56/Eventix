package com.tech.eventix.api.model

data class Dates(
    val start: StartDate,
    val timezone: String,
    val status: Status,
    val spanMultipleDays: Boolean
)

data class StartDate(
    val localDate: String,
    val localTime: String?,
    val dateTime: String,
    val dateTBD: Boolean,
    val dateTBA: Boolean,
    val timeTBA: Boolean,
    val noSpecificTime: Boolean
)

data class Status(
    val code: String
)