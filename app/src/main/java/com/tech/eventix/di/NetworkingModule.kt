package com.tech.eventix.di

import android.content.Context
import com.indiedev.networking.NetworkingKit
import com.indiedev.networking.contracts.GatewayBaseUrls
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkingModule {

    @Provides
    @Singleton
    fun provideNetworkingKit(
        @ApplicationContext context: Context,
    ): NetworkingKit {
        return NetworkingKit.builder(context)
            .gatewayUrls(createGatewayUrls())
            .build()
    }

    private fun createGatewayUrls(): GatewayBaseUrls {
        return object : GatewayBaseUrls {
            override fun getMainGatewayUrl(): String = "https://app.ticketmaster.com/"
        }
    }

}