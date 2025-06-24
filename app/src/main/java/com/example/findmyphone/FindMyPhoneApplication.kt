package com.example.findmyphone

import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FindMyPhoneApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
//        if (!Python.isStarted()) {
//            Python.start(AndroidPlatform(this))
//        }
    }
}