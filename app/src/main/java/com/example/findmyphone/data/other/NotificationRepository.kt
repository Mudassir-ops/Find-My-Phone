package com.example.findmyphone.data.other

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val notificationBuilder: NotificationCompat.Builder,
) {
    fun buildNotification(): NotificationCompat.Builder {
        return notificationBuilder
    }

}