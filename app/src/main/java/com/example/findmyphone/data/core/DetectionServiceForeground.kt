package com.example.findmyphone.data.core

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.ServiceCompat
import com.example.findmyphone.data.other.DetectionRepository
import com.example.findmyphone.data.other.NotificationRepository
import com.example.findmyphone.utils.Logs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class DetectionServiceForeground : Service(), OnSignalsDetectedListener {

    private var detectorThread: DetectorThread? = null
    private var handlerThread: HandlerThread? = null
    private var mServiceLooper: Looper? = null
    private var recorderThread: RecorderThread? = null
    private var lastWhistleTime: Long = 0
    private val whistleCooldown: Long = 10_000

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var detectionRepository: DetectionRepository

    override fun onCreate() {
        super.onCreate()
        val notificationBuilder = notificationRepository.buildNotification()
        val notification = notificationBuilder.build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= 34) {
                ServiceCompat.startForeground(
                    this@DetectionServiceForeground,
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
        startHandler()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun startHandler() {
        val handlerThread2 = HandlerThread("ServiceStartArguments", 10)
        this.handlerThread = handlerThread2
        handlerThread2.start()
        this.mServiceLooper = handlerThread?.looper
    }

    override fun onStartCommand(intent: Intent, i: Int, i2: Int): Int {
        startDetection()
        Log.d("DetectionService", "Service onStartCommand")
        return START_STICKY
    }

    private fun startDetection() {
        val recorderThread2 = RecorderThread()
        this.recorderThread = recorderThread2
        recorderThread2.start()

        val detectorThread2 = DetectorThread(this.recorderThread ?: return)
        this.detectorThread = detectorThread2
        detectorThread2.setOnSignalsDetectedListener(this)
        detectorThread?.start()

    }

    override fun onDestroy() {
        Log.d("DetectionService", "Service onDestroy")
        val recorderThread2 = this.recorderThread
        if (recorderThread2 != null) {
            recorderThread2.stopRecording()
            this.recorderThread = null
        }
        val detectorThread2 = this.detectorThread
        if (detectorThread2 != null) {
            detectorThread2.stopDetection()
            this.detectorThread = null
        }
        Log.d("DetectionService", "Service destroyed")
        detectionRepository.clearResources(context = this)
    }

    override fun onWhistleDetected() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastWhistleTime < whistleCooldown) {
            Logs.createLog("onWhistleIgnored: within cooldown")
            return
        }
        lastWhistleTime = currentTime
        Logs.createLog("onWhistleDetected: proceeding")
        detectionRepository.onWhistleDetected(context = this)
    }

    override fun onClapDetected() {
        Log.d("onClapDetected", "onClapDetected: ")
        detectionRepository.onClapDetected(context = this)
    }
}