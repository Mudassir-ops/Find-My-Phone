package com.example.findmyphone.di

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.findmyphone.MainActivity
import com.example.findmyphone.R
import com.example.findmyphone.data.other.NotificationRepository
import com.example.findmyphone.utils.AppConstants.FIND_MY_PHONE_ID
import com.example.findmyphone.utils.AppConstants.FIND_MY_PHONE_CHANNEL_NAME
import com.example.findmyphone.utils.AppConstants.PARENTAL_LOCK_CHANNEL_ID
import com.example.findmyphone.utils.AppConstants.PARENTAL_LOCK_CHANNEL_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {
    @Singleton
    @Provides
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManagerCompat {
        val notificationManager = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                PARENTAL_LOCK_CHANNEL_ID,
                PARENTAL_LOCK_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
        return notificationManager
    }


    @Singleton
    @Provides
    fun provideNotificationBuilder(
        @ApplicationContext context: Context
    ): NotificationCompat.Builder {
        val mainActivityIntent = Intent(context, MainActivity::class.java)
        mainActivityIntent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendingDefaultIntent = PendingIntent.getActivity(
            context, 0,
            mainActivityIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, PARENTAL_LOCK_CHANNEL_ID)
            .setSmallIcon(R.drawable.find_my_phone_app_icon_notification).setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(pendingDefaultIntent)
            .setContentTitle("Find MyPhone")
            .setContentText("Find MyPhone service is active")
            .setSound(null).setOngoing(true)
    }

    /** Notification repository provider*/
    @Singleton
    @Provides
    fun provideNotificationRepository(
        notificationBuilder: NotificationCompat.Builder,
        notificationManager: NotificationManagerCompat,
        @ApplicationContext context: Context
    ): NotificationRepository {
        return NotificationRepository(notificationBuilder)
    }

}


