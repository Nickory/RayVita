package com.codelab.basiclayouts.di

import com.codelab.basiclayouts.data.api.HealthApiService
import com.codelab.basiclayouts.network.RetrofitClient
import com.codelab.basiclayouts.network.model.HealthApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideHealthApiService(): HealthApiService {
        return RetrofitClient.healthApiService
    }
}