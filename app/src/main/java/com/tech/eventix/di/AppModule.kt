package com.tech.eventix.di

import android.content.Context
import com.tech.eventix.api.RemoteDataSource
import com.tech.eventix.repository.EventRepository
import com.tech.eventix.repository.EventRepositoryImpl
import com.tech.eventix.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import com.tech.eventix.logging.FlipperInterceptorFactory
import com.tech.eventix.repository.ApiKeyProvider
import com.tech.eventix.repository.BuildConfigApiKeyProvider

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context
    ): OkHttpClient {
        return OkHttpClient.Builder().apply {
            if (BuildConfig.DEBUG) {
                val flipperInterceptor = FlipperInterceptorFactory.createInterceptor(context)
                flipperInterceptor?.let {
                    addNetworkInterceptor(it)
                }
            }
        }.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://app.ticketmaster.com/") // Use your actual base URL
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

    @Provides
    @Singleton
    fun provideRemoteDataSource(retrofit: Retrofit): RemoteDataSource =
        retrofit.create(RemoteDataSource::class.java)

    @Provides
    @Singleton
    fun provideApiKeyProvider(): ApiKeyProvider = BuildConfigApiKeyProvider()

    @Provides
    @Singleton
    fun provideEventRepository(
        remoteDataSource: RemoteDataSource,
        apiKeyProvider: ApiKeyProvider,
        @ApplicationContext context: Context
    ): EventRepository =
        EventRepositoryImpl(remoteDataSource, apiKeyProvider, context)
} 