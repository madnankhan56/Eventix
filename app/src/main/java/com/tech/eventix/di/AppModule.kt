package com.tech.eventix.di

import com.indiedev.networking.api.GatewaysBaseUrls
import com.indiedev.networking.api.NetworkExternalAPI
import com.indiedev.networking.api.NetworkExternalDependencies
import com.tech.eventix.api.RemoteDataSource
import com.tech.eventix.repository.ApiKeyProvider
import com.tech.eventix.repository.BuildConfigApiKeyProvider
import com.tech.eventix.repository.EventRepository
import com.tech.eventix.repository.EventRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNetworkExternalDependencies(): NetworkExternalDependencies =
        object : NetworkExternalDependencies {
            override fun getBaseUrls(): GatewaysBaseUrls {
                return object : GatewaysBaseUrls {
                    override fun getMainGatewayUrl(): String = "https://app.ticketmaster.com/"
                }
            }
        }

    @Provides
    @Singleton
    fun provideRemoteDataSource(networkExternalAPI: NetworkExternalAPI): RemoteDataSource =
        networkExternalAPI.createServiceOnMainGateway(RemoteDataSource::class.java)

    @Provides
    @Singleton
    fun provideApiKeyProvider(): ApiKeyProvider = BuildConfigApiKeyProvider()

    @Provides
    @Singleton
    fun provideEventRepository(
        remoteDataSource: RemoteDataSource,
        apiKeyProvider: ApiKeyProvider
    ): EventRepository =
        EventRepositoryImpl(remoteDataSource, apiKeyProvider)
} 