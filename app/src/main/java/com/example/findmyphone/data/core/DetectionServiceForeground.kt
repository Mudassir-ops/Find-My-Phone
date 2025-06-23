package com.example.findmyphone.data.core

import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
import android.os.Build
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ServiceCompat
import com.example.findmyphone.data.other.DetectionRepository
import com.example.findmyphone.data.other.NotificationRepository
import com.example.findmyphone.utils.Logs
import com.example.findmyphone.utils.SessionManager
import com.example.findmyphone.utils.isCurrentTimeInRange
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

@AndroidEntryPoint
class DetectionServiceForeground1 : Service(), OnSignalsDetectedListener {

    private var detectorThread: DetectorThread? = null
    private var handlerThread: HandlerThread? = null
    private var mServiceLooper: Looper? = null
    private var recorderThread: RecorderThread? = null
    private var lastWhistleTime: Long = 0
    private val whistleCooldown: Long = 5000
    private var timer: Timer? = null
    private var lastApp: String? = null


    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var detectionRepository: DetectionRepository

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate() {
        super.onCreate()
        val notificationBuilder = notificationRepository.buildNotification()
        val notification = notificationBuilder.build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= 34) {
                ServiceCompat.startForeground(
                    this@DetectionServiceForeground1,
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
        // startTimer()
        Log.d("DetectionService", "Service onStartCommand")
        return START_STICKY
    }

    private fun startDetection() {
        val recorderThread2 = RecorderThread()
        this.recorderThread = recorderThread2
        recorderThread2.start()
        val detectorThread2 = DetectorThread(this.recorderThread ?: return, sessionManager, this)
        this.detectorThread = detectorThread2
        detectorThread2.setOnSignalsDetectedListener(this)
        detectorThread?.start()

    }

    override fun onDestroy() {
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
        stopTimer()
    }

    override fun onWhistleDetected() {
        if (onDeactivation()) return
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastWhistleTime < whistleCooldown) {
            Logs.createLog("onWhistleIgnored: within cooldown$lastWhistleTime")
            return
        }
        lastWhistleTime = currentTime
        Logs.createLog("onWhistleDetected: proceeding")
        detectionRepository.onWhistleDetected(context = this)
    }

    override fun onClapDetected() {
        if (onDeactivation()) return
        Log.d("onClapDetected", "onClapDetected: ")
        detectionRepository.onClapDetected(context = this)
    }

    private fun getCurrentApp() {
        Log.d("onClapDetected", "getCurrentApp: ")
        val usageStatsManager = getSystemService(UsageStatsManager::class.java)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR, -1)
        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, calendar.timeInMillis, System.currentTimeMillis()
        )
        Log.d("onClapDetected", "getCurrentApp:$usageStatsList ")
        if (usageStatsList.isNotEmpty()) {
            val sortedList = usageStatsList.sortedByDescending { it.lastTimeUsed }
            val currentApp = sortedList.firstOrNull()?.packageName
            if (currentApp != lastApp) {
                lastApp = currentApp
                sessionManager.setCurrentAppForClapDetection(appName = currentApp ?: "")
                Log.d("Current App", "Package ifExists:getCurrentApp---> $currentApp")
            } else {
                Log.d("Current App", "Same app detected: $currentApp, no action taken.")
            }
            Log.d("Current App", "Package: $currentApp")
        }
    }

    private fun startTimer() {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                getCurrentApp()
            }
        }, 0, 100)
    }

    private fun stopTimer() {
        timer?.cancel()
    }

    private fun onDeactivation(): Boolean {
        val isDeActivationTurnedOn = sessionManager.getDeactivationMode()
        val deactivationStartTime = sessionManager.getStartTime()
        val deactivationEndTime = sessionManager.getEndTime()
        val isCurrentTimeInRange =
            isCurrentTimeInRange(deactivationStartTime ?: "00:00", deactivationEndTime ?: "00:00")
        Logs.createLog("deactivationEndTime--$isDeActivationTurnedOn--$isCurrentTimeInRange")
        Logs.createLog("deactivationEndTime--$deactivationStartTime--$deactivationEndTime")
        return isDeActivationTurnedOn == true && isCurrentTimeInRange
    }

}