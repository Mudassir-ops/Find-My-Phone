package com.example.findmyphone.di

import android.content.Context
import android.content.SharedPreferences
import com.example.findmyphone.data.other.DetectionRepository
import com.example.findmyphone.domain.repository.DetectionRepositoryImpl
import com.example.findmyphone.utils.SessionManager
import com.example.findmyphone.utils.AppConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FindMyPhoneAppModule {

    @Provides
    @Singleton
    fun provideDetectionRepository(): DetectionRepository {
        return DetectionRepositoryImpl()
    }

    @Singleton
    @Provides
    fun sessionManager(preferences: SharedPreferences?) =
        SessionManager(preferences)

    @Singleton
    @Provides
    fun provideFindMyPhonePreferences(@ApplicationContext context: Context): SharedPreferences? =
        context.getSharedPreferences(
            AppConstants.PREF_NAME, Context.MODE_PRIVATE
        )
}