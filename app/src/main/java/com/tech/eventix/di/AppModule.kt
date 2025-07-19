package com.tech.eventix.di

import com.tech.eventix.api.RemoteDataSource
import com.tech.eventix.repository.EventRepository
import com.tech.eventix.repository.EventRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://app.ticketmaster.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideRemoteDataSource(retrofit: Retrofit): RemoteDataSource =
        retrofit.create(RemoteDataSource::class.java)

    @Provides
    @Singleton
    fun provideEventRepository(remoteDataSource: RemoteDataSource): EventRepository =
        EventRepositoryImpl(remoteDataSource)
} 