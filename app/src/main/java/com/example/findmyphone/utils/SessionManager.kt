package com.example.findmyphone.utils

import android.content.SharedPreferences
import com.example.findmyphone.utils.AppConstants.DEACTIVATION_TIME
import com.example.findmyphone.utils.AppConstants.DETECTION_MODE
import com.example.findmyphone.utils.AppConstants.FLASH_LIGHT_THRESHOLD
import com.example.findmyphone.utils.AppConstants.IS_FLASH_LIGHT_ON
import com.example.findmyphone.utils.AppConstants.MY_RINGTONE
import javax.inject.Inject

class SessionManager @Inject constructor(private val preferences: SharedPreferences?) {

    private inline fun <reified T> putValue(key: String, value: T) {
        val editor = preferences?.edit()
        when (value) {
            is Int -> editor?.putInt(key, value)
            is Boolean -> editor?.putBoolean(key, value)
            is String -> editor?.putString(key, value)
            else -> throw IllegalArgumentException("Unsupported type")
        }
        editor?.apply()
    }

    private inline fun <reified T> getValue(key: String, defaultValue: T): T? {
        return when (defaultValue) {
            is Int -> preferences?.getInt(key, defaultValue) as T
            is Boolean -> preferences?.getBoolean(key, defaultValue) as T
            is String -> preferences?.getString(key, defaultValue) as T
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }

    private inline fun <reified T> setPreference(key: String, value: T) {
        putValue(key, value)
    }

    private inline fun <reified T> getPreference(key: String, defaultValue: T): T? {
        return getValue(key, defaultValue)
    }


    // Ringtone preference
    fun setRingtone(ringtone: String) {
        setPreference(MY_RINGTONE, ringtone)
    }

    fun getRingtone(): String? {
        return getPreference(MY_RINGTONE, "")
    }

    // Flashlight state preference
    fun setFlashlightState(isOn: Boolean) {
        setPreference(IS_FLASH_LIGHT_ON, isOn)
    }

    fun isFlashlightOn(): Boolean? {
        return getPreference(IS_FLASH_LIGHT_ON, false)
    }

    // Detection mode preference
    fun setDetectionMode(mode: String) {
        setPreference(DETECTION_MODE, mode)
    }

    fun getDetectionMode(): String? {
        return getPreference(DETECTION_MODE, "")
    }

    // Flashlight threshold preference
    fun setFlashlightThreshold(threshold: Int) {
        setPreference(FLASH_LIGHT_THRESHOLD, threshold)
    }

    fun getFlashlightThreshold(): Int? {
        return getPreference(FLASH_LIGHT_THRESHOLD, 0)
    }

    // Deactivation time preference
    fun setDeactivationTime(time: Long) {
        setPreference(DEACTIVATION_TIME, time)
    }

    fun getDeactivationTime(): Long? {
        return getPreference(DEACTIVATION_TIME, 0L)
    }

}
