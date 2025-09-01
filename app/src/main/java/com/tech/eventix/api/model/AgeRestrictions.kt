package com.tech.eventix.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AgeRestrictions(
    val legalAgeEnforced: Boolean,
    val id: String?= null
)