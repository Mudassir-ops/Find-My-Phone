package com.example.findmyphone.data.other

import android.content.Context

interface DetectionRepository {
    fun onWhistleDetected(context: Context)
    fun onClapDetected(context: Context)
    fun clearResources(context: Context)
}