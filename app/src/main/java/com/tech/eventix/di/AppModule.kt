package com.tech.eventix.di

import com.indiedev.networking.NetworkingKit
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
    fun provideRemoteDataSource(networkExternalAPI: NetworkingKit): RemoteDataSource =
        networkExternalAPI.createMainService(RemoteDataSource::class.java)

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