package com.example.findmyphone.utils

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes

import android.os.Handler
import android.os.Looper

class MediaPlayerManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private val stopRunnable = Runnable { stop() }

    fun play(@RawRes resId: Int, durationMs: Long = 5000L) {
        stop()
        mediaPlayer = MediaPlayer.create(context, resId).apply {
            isLooping = false
            setOnCompletionListener {
                stop()
            }
            start()
        }
        handler.postDelayed(stopRunnable, durationMs)
    }

    fun pause() {
        mediaPlayer?.takeIf { it.isPlaying }?.pause()
        handler.removeCallbacks(stopRunnable)
    }

    fun resume() {
        mediaPlayer?.start()
        handler.postDelayed(stopRunnable, 5000L)
    }

    fun stop() {
        handler.removeCallbacks(stopRunnable)
        mediaPlayer?.apply {
            stop()
            reset()
            release()
        }
        mediaPlayer = null
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    fun release() {
        stop()
    }
}

