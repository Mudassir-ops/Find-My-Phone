package com.example.findmyphone.data.core

import android.util.Log
import com.example.findmyphone.utils.Logs.createLog
import com.musicg.api.ClapApi
import com.musicg.api.WhistleApi
import com.musicg.wave.WaveHeader
import java.util.LinkedList
import kotlin.concurrent.Volatile

class DetectorThread(private val recorder: RecorderThread) : Thread() {

    @Volatile
    private var running = true
    private var numWhistles = 0
    private var numClaps = 0

    private val whistleApi: WhistleApi
    private val clapApi: ClapApi
    private val whistleResultList = LinkedList<Boolean>()
    private val clapResultList = LinkedList<Boolean>()

    private var onSignalsDetectedListener: OnSignalsDetectedListener? = null

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
                    Log.e("Sound", "Clap Detected")
                    initBuffer()
                    onSignalsDetectedListener?.onClapDetected()
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

}

