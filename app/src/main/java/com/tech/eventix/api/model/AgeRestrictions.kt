package com.tech.eventix.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AgeRestrictions(
    val legalAgeEnforced: Boolean? = null,
    val id: String?= null
)