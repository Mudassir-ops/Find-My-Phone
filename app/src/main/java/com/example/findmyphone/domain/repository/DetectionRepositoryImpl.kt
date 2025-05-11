package com.example.findmyphone.domain.repository

import android.app.Service.CAMERA_SERVICE
import android.app.Service.VIBRATOR_SERVICE
import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.findmyphone.data.other.DetectionRepository
import com.example.findmyphone.utils.Logs
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

class DetectionRepositoryImpl : DetectionRepository {

    private val _isServiceRunningStateFlow = MutableStateFlow(false)
    override val isServiceRunningStateFlow: StateFlow<Boolean> =
        _isServiceRunningStateFlow.asStateFlow()


    private val flashDelay = 1200L
    private val vibrationDuration = 900L
    private val clapThreshold = 500L

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
        if (isDoubleClap) {
            playRingtone(context)
            vibrateDevice(context)
            flashSequence(context)
        }
    }

    private fun handleWhistleDetectionLogic(context: Context) {
        if (isWhistleProcessing) return
        isWhistleProcessing = true
        Logs.createLog("handleWhistleDetectionLogic---> triggered")
        playRingtone(context)
        vibrateDevice(context)
        flashSequence(context)
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

        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(context, ringtoneUri)
                setAudioStreamType(AudioManager.STREAM_RING)
                setOnCompletionListener {
                    stop()
                    reset()
                    release()
                    mediaPlayer = null
                }
                prepare()
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
                delay(flashDelay)
                toggleFlashLight(false)
                delay(flashDelay)
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


