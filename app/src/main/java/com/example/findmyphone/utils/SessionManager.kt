package com.example.findmyphone.utils

import android.content.SharedPreferences
import com.example.findmyphone.R
import com.example.findmyphone.utils.AppConstants.CURRENT_APP_FOR_DETECTION
import com.example.findmyphone.utils.AppConstants.DEACTIVATION_TIME
import com.example.findmyphone.utils.AppConstants.DETECTION_MODE
import com.example.findmyphone.utils.AppConstants.FLASH_LIGHT_THRESHOLD
import com.example.findmyphone.utils.AppConstants.IS_FLASH_LIGHT_ON
import com.example.findmyphone.utils.AppConstants.MY_RINGTONE
import com.example.findmyphone.utils.AppConstants.SOUND_SENSITIVITY_LEVEL
import com.example.findmyphone.utils.AppConstants.VOLUME_LEVEL
import javax.inject.Inject

class SessionManager @Inject constructor(private val preferences: SharedPreferences?) {

    private inline fun <reified T> putValue(key: String, value: T) {
        val editor = preferences?.edit()
        when (value) {
            is Int -> editor?.putInt(key, value)
            is Boolean -> editor?.putBoolean(key, value)
            is String -> editor?.putString(key, value)
            is Long -> editor?.putLong(key, value)
            else -> throw IllegalArgumentException("Unsupported type")
        }
        editor?.apply()
    }

    private inline fun <reified T> getValue(key: String, defaultValue: T): T? {
        return when (defaultValue) {
            is Int -> preferences?.getInt(key, defaultValue) as T
            is Boolean -> preferences?.getBoolean(key, defaultValue) as T
            is String -> preferences?.getString(key, defaultValue) as T
            is Long -> preferences?.getLong(key, defaultValue) as T
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
    fun setRingtone(ringtone: Int) {
        setPreference(MY_RINGTONE, ringtone)
    }

    fun getRingtone(): Int? {
        return getPreference(MY_RINGTONE, R.raw.door_bell)
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
    fun setFlashlightThreshold(threshold: Long) {
        setPreference(FLASH_LIGHT_THRESHOLD, threshold)
    }

    fun getFlashlightThreshold(): Long? {
        return getPreference(FLASH_LIGHT_THRESHOLD, 400L)
    }

    // Deactivation time preference
    fun setDeactivationTime(time: Long) {
        setPreference(DEACTIVATION_TIME, time)
    }

    fun getDeactivationTime(): Long? {
        return getPreference(DEACTIVATION_TIME, 0L)
    }

    // Volume  preference
    fun setVolumeLevel(time: Int) {
        setPreference(VOLUME_LEVEL, time)
    }

    fun getVolumeLevel(): Int? {
        return getPreference(VOLUME_LEVEL, 40)
    }

    // Volume  preference
    fun setSoundSensitivityLevel(time: Int) {
        setPreference(SOUND_SENSITIVITY_LEVEL, time)
    }

    fun getSoundSensitivityLevel(): Int? {
        return getPreference(SOUND_SENSITIVITY_LEVEL, 0)
    }

    //--Current App For Clap Detection
    fun setCurrentAppForClapDetection(appName: String) {
        setPreference(CURRENT_APP_FOR_DETECTION, appName)
    }

    fun getCurrentAppForClapDetection(): String? {
        return getPreference(CURRENT_APP_FOR_DETECTION, "com.example.findmyphone")
    }

}
