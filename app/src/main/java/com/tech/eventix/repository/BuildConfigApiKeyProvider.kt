package com.tech.eventix.repository

import com.tech.eventix.BuildConfig
import javax.inject.Inject

class BuildConfigApiKeyProvider @Inject constructor() : ApiKeyProvider {
    override fun getApiKey(): String = BuildConfig.API_KEY
} 