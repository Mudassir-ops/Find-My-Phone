package com.example.findmyphone.data.local

import android.content.SharedPreferences


enum class PrefMigrate(val newVersion: Int) {
    M_0_1(1);

    fun migrate() {
        when (this) {
            M_0_1 -> {

            }
        }
    }
}

enum class Settings {

    PASS_TUTORIAL,
    VERSION_P,
    APP_LANGUAGE,
    RATE,
    SOUND_POSITION,
    TIME_SOUND, FLASH_LIGHT,
    VIBRATION, VOLUME,
    PASS_TUTORIAL_HOME,
    SENSITIVITY,
    START_UNPLUGGING_ALARM,
    CUSTOM_SOUND;


    companion object {

        const val VERSION = 1


        fun migrate() {
            PrefMigrate.values().filter { it.newVersion <= VERSION }.sortedBy { it.newVersion }
                .forEach { it.migrate() }
        }
    }
}

inline fun <reified T> SharedPreferences.get(key: String, defaultValue: T): T {
    when (T::class) {
        Boolean::class -> return this.getBoolean(key, defaultValue as Boolean) as T
        Float::class -> return this.getFloat(key, defaultValue as Float) as T
        Int::class -> return this.getInt(key, defaultValue as Int) as T
        Long::class -> return this.getLong(key, defaultValue as Long) as T
        String::class -> return this.getString(key, defaultValue as String) as T
        else -> {
            if (defaultValue is Set<*>) {
                return this.getStringSet(key, defaultValue as Set<String>) as T
            }
        }
    }

    return defaultValue
}

inline fun <reified T> SharedPreferences.put(key: String, value: T) {
    val editor = this.edit()
    when (T::class) {
        Boolean::class -> editor.putBoolean(key, value as Boolean)
        Float::class -> editor.putFloat(key, value as Float)
        Int::class -> editor.putInt(key, value as Int)
        Long::class -> editor.putLong(key, value as Long)
        String::class -> editor.putString(key, value as String)
        else -> {
            if (value is Set<*>) {
                editor.putStringSet(key, value as Set<String>)
            }
        }
    }

    editor.apply()
}