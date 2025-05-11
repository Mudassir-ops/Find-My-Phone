package com.example.findmyphone.data.other

import android.content.Context
import kotlinx.coroutines.flow.StateFlow

interface DetectionRepository {
    val isServiceRunningStateFlow: StateFlow<Boolean>

    fun onWhistleDetected(context: Context)
    fun onClapDetected(context: Context)
    fun clearResources(context: Context)

    fun isServiceRunning(isServiceRunning: Boolean)

}