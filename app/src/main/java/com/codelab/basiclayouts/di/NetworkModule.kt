package com.codelab.basiclayouts.di

import android.content.Context
import com.codelab.basiclayouts.data.UserSessionManager
import com.codelab.basiclayouts.network.AuthApi
import com.codelab.basiclayouts.network.RetrofitClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideUserSessionManager(
        @ApplicationContext context: Context

    ): UserSessionManager {
        return UserSessionManager(context)
    }

    @Provides
    @Singleton
    fun provideAuthApi(): AuthApi {
        return RetrofitClient.authApi
    }
}