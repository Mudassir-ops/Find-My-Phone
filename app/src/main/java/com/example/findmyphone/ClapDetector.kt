package com.example.findmyphone

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.onsets.PercussionOnsetDetector
import be.tarsos.dsp.pitch.PitchDetectionResult
import be.tarsos.dsp.util.fft.FFT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ClapDetector(
    private val context: Context,
    private val onClapDetected: (Double, Double) -> Unit,
    private val threshold: Double = 2.0, // Lowered to detect close-range claps
    private val sensitivity: Double = 10.0 // Lowered for sharp onsets
) {
    private var dispatcher: AudioDispatcher? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private var lastOnsetTime: Double? = null // Track last onset time
    private val debounceWindow = 1.0 // 1-second debounce window

    fun startClapDetection() {
        if (dispatcher != null) {
            Log.w(TAG, "Clap detection already running")
            return
        }
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Microphone permission not granted")
            return
        }

        val sampleRate = 22050
        val bufferSize = 1024
        val bufferOverlap = 256

        try {
            dispatcher =
                AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, bufferSize, bufferOverlap)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize microphone", e)
            return
        }

        val clapDetector = PercussionOnsetDetector(
            sampleRate.toFloat(),
            bufferSize,
            { time, salience ->
                Log.d(TAG, "Raw Onset: $time -- $salience")
            },
            5.0,  // High sensitivity
            1.0   // Low threshold
        )

        dispatcher?.addAudioProcessor(clapDetector)
        scope.launch {
            dispatcher?.run()
        }
    }

    fun stopClapDetection() {
        dispatcher?.stop()
        dispatcher = null
        lastOnsetTime = null
    }

    companion object {
        private const val TAG = "ClapDetector"
    }


}



