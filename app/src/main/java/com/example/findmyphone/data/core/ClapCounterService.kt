package com.example.findmyphone.data.core

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.ServiceCompat
import com.findmyphone.clapping.clapfinder.soundalert.R
import com.example.findmyphone.data.other.NotificationRepository
import com.example.findmyphone.utils.hasPermissions
import com.example.findmyphone.data.local.Settings
import com.example.findmyphone.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class ClapCounterService : Service() {

    private var clapDetector: ClapDetector? = null
    private var bufferSize = 1024

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var sessionManager: SessionManager


    private fun startListener() {
        Log.d("sattiNaju--->", "startListener: ")
        if (hasPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO))) {
            Log.d("sattiNaju--->", "startListener:51 ")
            clapDetector?.cancel()
            clapDetector?.startDetectClap(
                sensitivity = 40.0,
                action = {
                    Log.d("sattiNajuAction--->", "startListener:51$isPlaySound ")
                    CoroutineScope(Dispatchers.Main).launch {
                        if (!isPlaySound) {
                            startSound()
                        }
                    }
                },
                bufferSize = bufferSize,
                onError = {
                    bufferSize *= 2
                    startListener()
                })
        } else {
            Log.d("sattiNaju--->", "startListener:71 ")
            stopSelf()
        }
    }

    private fun stopListener() {
        clapDetector?.cancel()
    }

    override fun onCreate() {
        super.onCreate()
        val notificationBuilder = notificationRepository.buildNotification()
        val notification = notificationBuilder.build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= 34) {
                ServiceCompat.startForeground(
                    this,
                    1,
                    notification,
                    FOREGROUND_SERVICE_TYPE_MICROPHONE
                )
            } else {
                startForeground(1, notification)
            }
        } else {
            startForeground(1, notification)
        }
        clapDetector = ClapDetector()
        startListener()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startListener()
        return START_NOT_STICKY
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onDestroy() {
        super.onDestroy()
        stopSound()
        stopListener()
    }

    private var vibrator: Vibrator? = null
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null
    private var handler: Handler? = null
    private var countDownTimer: CountDownTimer? = null
    private val mediaPlayer: ManagerAudio by lazy {
        ManagerAudio(context = this, sessionManager = sessionManager)
    }
    private var originalVolume: Int? = null
    private var isPlaySound: Boolean = false

    private fun startSound() {
        isPlaySound = true
        val mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val volume = mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        originalVolume = volume
        mAudioManager.setStreamVolume(
            AudioManager.STREAM_ALARM,
            100,
            0
        )
        handler = Handler(Looper.getMainLooper())
        val selectedRingtone = sessionManager.getRingtone()
        val soundUri = Uri.parse("android.resource://${this.packageName}/$selectedRingtone")
        mediaPlayer.play(soundUri.toString()) {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }
            countDownTimer = object : CountDownTimer(15000L, 5000) {
                override fun onTick(p0: Long) {
                    vibrator?.cancel()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(
                            VibrationEffect.createOneShot(
                                5000,
                                VibrationEffect.DEFAULT_AMPLITUDE
                            )
                        )
                    }
                }

                override fun onFinish() {
                    vibrator?.cancel()
                }
            }
            countDownTimer?.start()
            if (sessionManager.isFlashlightOn() == true) {
                try {
                    startFlashlightSequence(this)
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                }
            }
            handler?.postDelayed({
                stopSound()
            }, 5000L)
        }
    }

    private fun stopSound() {
        isPlaySound = false
        handler?.removeCallbacksAndMessages(null)
        mediaPlayer.release()
        countDownTimer?.cancel()
        vibrator?.cancel()
        if (cameraId != null) {
            try {
                cameraManager?.setTorchMode(cameraId ?: return, false)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }

        val mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val volume = originalVolume ?: mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        mAudioManager.setStreamVolume(
            AudioManager.STREAM_ALARM,
            volume,
            0
        )
    }

    private fun startFlashlightSequence(context: Context) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = getBackCameraWithFlash(cameraManager) ?: return
        CoroutineScope(Dispatchers.Main).launch {
            repeat(5) {
                cameraManager.setTorchMode(cameraId, true)
                delay(sessionManager.getFlashlightThreshold() ?: 400L)
                cameraManager.setTorchMode(cameraId, false)
                delay(sessionManager.getFlashlightThreshold() ?: 400L)
            }
        }
    }

    private fun getBackCameraWithFlash(cameraManager: CameraManager): String? {
        try {
            val cameraIds = cameraManager.cameraIdList
            for (id in cameraIds) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val flashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
                val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (flashAvailable == true && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    return id
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}

enum class ClapCounterAction(val value: Int) {
    STOP(2),
    CLOSE(3),
    RESET(4);

    companion object {
        fun getValue(int: Int?): ClapCounterAction? {
            return values().firstOrNull { it.value == int }
        }
    }

}


enum class Duration {
    D15S, D30S, D1M, D2M;

    val duration: Long
        get() {
            return when (this) {
                D15S -> 5000L
                D30S -> 30000L
                D1M -> 60000L
                D2M -> 2 * 60000L
            }
        }

    companion object {
        fun get(position: Int) = Duration.values().getOrNull(position)
        fun index(duration: Duration): Int = Duration.values().indexOf(duration)
    }


}
