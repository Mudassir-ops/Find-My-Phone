package com.example.findmyphone.data.core

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.findmyphone.utils.Logs
import com.example.findmyphone.utils.Logs.createLog
import com.musicg.api.ClapApi
import com.musicg.api.WhistleApi
import com.musicg.wave.WaveHeader
import java.util.LinkedList
import kotlin.concurrent.Volatile
import kotlin.math.absoluteValue

@SuppressLint("MissingPermission")
class DetectorThread(private val recorder: RecorderThread) : Thread() {

    @Volatile
    private var running = true
    private var numWhistles = 0
    private var numClaps = 0

    private val whistleApi: WhistleApi
    private val clapApi: ClapApi
    private val whistleResultList = LinkedList<Boolean>()
    private val clapResultList = LinkedList<Boolean>()
    val clapCooldownMillis = 1500L // 1.5-second cooldown between claps
    var lastClapDetectedTime = 0L
    private var onSignalsDetectedListener: OnSignalsDetectedListener? = null
    var previousAmplitude = 0.0
    val threshold = 50

    init {
        val audioRecord = recorder.audioRecord
        val bits = when (audioRecord.audioFormat) {
            2 -> 16
            3 -> 8
            else -> 0
        }
        val channels = if (audioRecord.channelConfiguration == 16) 1 else 0

        val waveHeader = WaveHeader().apply {
            this.channels = channels
            this.bitsPerSample = bits
            this.sampleRate = audioRecord.sampleRate
        }
        Logs.createLog("waveThread-->${waveHeader}-----")
        Logs.createLog("waveThread-->${channels}-----")
        Logs.createLog("waveThread-->${bits}-----")

        whistleApi = WhistleApi(waveHeader)
        clapApi = ClapApi(waveHeader)

    }

    private fun initBuffer() {
        numWhistles = 0
        numClaps = 0
        whistleResultList.clear()
        clapResultList.clear()
        repeat(3) { whistleResultList.add(false) }
        repeat(1) { clapResultList.add(false) }
    }

    override fun run() {
        initBuffer()
        try {
            while (running) {
                val frameBytes = recorder.frameBytes
                val oldWhistle = whistleResultList.removeFirst()
                val oldClap = clapResultList.removeFirst()
                if (oldWhistle) numWhistles--
                if (oldClap) numClaps--
                val isWhistle = frameBytes?.let { whistleApi.isWhistle(it) } ?: false
                val isClap = frameBytes?.let { clapApi.isClap(it) } ?: false
                Log.e("isClapDetectedSatti", "Clap Detected$isClap")
                if (isClap) {
                    //  processClap(frameBytes)
                    Log.e("isClapDetectedSattiTrue", "Clap Detected$isClap")
                }
                whistleResultList.add(isWhistle)
                clapResultList.add(isClap)
                if (isWhistle) numWhistles++
                if (isClap) numClaps++
                createLog("DetailOfAudio", "$numWhistles sands $numClaps")
                createLog("DetailOfAudioCheck", "$isWhistle sands $isClap")
                if (numWhistles >= 3) {
                    Log.e("Sound", "Whistle Detected")
                    initBuffer()
                    onSignalsDetectedListener?.onWhistleDetected()
                }
                if (numClaps >= 1) {
                    Log.d("ClapLogic", "Started possible clap phase at $clapStartedAt")
                    onPossibleClapDetected1(clapStartedAt)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopDetection() {
        running = false
    }

    fun setOnSignalsDetectedListener(listener: OnSignalsDetectedListener?) {
        onSignalsDetectedListener = listener
    }

    private var clapStartedAt = 0L
    private val clapTimestamps = ArrayDeque<Long>()
    private val clapWindowMs = 600L
    private fun onPossibleClapDetected1(currentTime: Long) {
        // Add the new sound timestamp
        clapTimestamps.addLast(currentTime)
        // Remove old timestamps outside the 3s window
        while (clapTimestamps.isNotEmpty() && currentTime - clapTimestamps.first() > clapWindowMs) {
            clapTimestamps.removeFirst()
        }
        Log.d("ClapLogic", "Clap timestamps: $clapTimestamps")
        if (clapTimestamps.size == 1) {
            Log.d("ClapLogic", "Started possible clap phase at ${clapTimestamps.first()}")
            Log.d("ClapLogic", "Waiting for more claps...")
            Handler(Looper.getMainLooper()).postDelayed({
                if (clapTimestamps.size == 1) {
                    initBuffer()
                    onSignalsDetectedListener?.onClapDetected()
                    Log.d("ClapLogic", "✅ Real Clap Detected!")
                    clapTimestamps.clear()
                } else {
                    Log.d("ClapLogic", "❌ Ignored - too many rapid claps (likely noise)!")
                    clapTimestamps.clear()
                }
            }, clapWindowMs)
        }
    }

}

