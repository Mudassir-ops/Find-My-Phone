package com.example.findmyphone.domain.repository

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.findmyphone.data.other.DetectionRepository
import com.example.findmyphone.utils.Logs
import com.example.findmyphone.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject


class DetectionRepositoryImpl @Inject constructor(private val sessionManager: SessionManager) :
    DetectionRepository {

    private val _isServiceRunningStateFlow = MutableStateFlow(false)
    override val isServiceRunningStateFlow: StateFlow<Boolean> =
        _isServiceRunningStateFlow.asStateFlow()

    private val vibrationDuration = 900L
    private val clapThreshold = 1000L

    private var lastClapTime = 0L
    private var isDoubleClap = false
    private var isVibrating = false
    private var isFlashOn = false
    private var isWhistleProcessing = false

    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null
    private var mediaPlayer: MediaPlayer? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)


    override fun onWhistleDetected(context: Context) {
        handleWhistleDetectionLogic(context)
    }

    override fun onClapDetected(context: Context) {
        handleDetectionLogic(context)
    }

    override fun clearResources(context: Context) {
        cleanup()
        stopVibrating(context = context)
    }

    private fun handleDetectionLogic(context: Context) {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastClap = currentTime - lastClapTime
        lastClapTime = currentTime
        isDoubleClap = timeSinceLastClap <= clapThreshold
        val currentVisibleApp = sessionManager.getCurrentAppForClapDetection()
        Logs.createLog("isDoubleClap---$isDoubleClap--$currentVisibleApp")
        if (isDoubleClap && (currentVisibleApp == (context.packageName ?: ""))) {
            playRingtone(context)
            vibrateDevice(context)
            if (sessionManager.isFlashlightOn() == true) {
                flashSequence(context)
            }
        }
    }

    private fun handleWhistleDetectionLogic(context: Context) {
        Logs.createLog("handleWhistleDetectionLogic---> triggered")
        playRingtone(context)
        vibrateDevice(context)
        if (sessionManager.isFlashlightOn() == true) {
            flashSequence(context)
        }
        scope.launch {
            delay(5000)
            isWhistleProcessing = false
        }
    }

    private fun playRingtone(context: Context) {
        if (mediaPlayer?.isPlaying == true) return

        mediaPlayer?.apply {
            stop()
            reset()
            release()
        }
        val selectedRingtone = sessionManager.getRingtone()
        val scaledVolume = when (val volumeLevel = sessionManager.getVolumeLevel()) {
            in 0..100 -> (volumeLevel?.toFloat() ?: 20F) / 100f
            else -> 0.5f
        }
        val uri = Uri.parse("android.resource://${context.packageName}/$selectedRingtone")
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(context, uri)
                setAudioStreamType(AudioManager.STREAM_RING)
                setOnCompletionListener {
                    stop()
                    reset()
                    release()
                    mediaPlayer = null
                }
                prepare()
                setVolume(scaledVolume, scaledVolume)
                start()
                scope.launch {
                    delay(5000)
                    if (isPlaying) {
                        stop()
                        reset()
                        release()
                        mediaPlayer = null
                    }
                }

            } catch (e: IOException) {
                e.printStackTrace()
                release()
                mediaPlayer = null
            }
        }
    }


    private fun vibrateDevice(context: Context) {
        if (isVibrating) return

        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION") context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= 26) {
                val effect = VibrationEffect.createWaveform(
                    longArrayOf(vibrationDuration, 2000, 3000, 2000), -1
                )
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION") vibrator.vibrate(
                    longArrayOf(
                        vibrationDuration,
                        2000,
                        3000,
                        2000
                    ), -1
                )
            }

            isVibrating = true
            scope.launch {
                delay(5000)
                stopVibrating(context = context)
            }
        }
    }

    private fun flashSequence(context: Context) {
        if (cameraManager == null || cameraId == null) initCamera(context)
        scope.launch(Dispatchers.IO) {
            repeat(3) {
                toggleFlashLight(true)
                delay(sessionManager.getFlashlightThreshold() ?: 400L)
                toggleFlashLight(false)
                delay(sessionManager.getFlashlightThreshold() ?: 400L)
            }
        }
    }

    private fun toggleFlashLight(state: Boolean) {
        try {
            cameraManager?.setTorchMode(cameraId ?: return, state)
            isFlashOn = state
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun initCamera(context: Context) {
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        cameraId = cameraManager?.cameraIdList?.firstOrNull()
    }

    private fun stopVibrating(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION") context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.cancel()
        isVibrating = false
    }

    private fun cleanup() {
        scope.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override suspend fun isServiceRunning(isServiceRunning: Boolean) {
        _isServiceRunningStateFlow.emit(isServiceRunning)
    }
}


