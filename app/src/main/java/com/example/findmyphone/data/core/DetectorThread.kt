package com.example.findmyphone.data.core

import android.annotation.SuppressLint
import android.util.Log
import com.example.findmyphone.TEST.musicg.api.ClapApi
import com.example.findmyphone.TEST.musicg.api.WhistleApi
import com.example.findmyphone.TEST.musicg.wave.WaveHeader
import com.example.findmyphone.utils.Logs
import com.example.findmyphone.utils.Logs.createLog
import com.example.findmyphone.utils.SessionManager
import java.util.LinkedList
import kotlin.concurrent.Volatile

@SuppressLint("MissingPermission")
class DetectorThread(
    private val recorder: RecorderThread,
    private val numOfClapSensitivity: SessionManager
) :
    Thread() {

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
            else -> 8
        }
        val channels = if (audioRecord.channelConfiguration == 16) 1 else 0

        val waveHeader = WaveHeader().apply {
            chunkId = "RIFF"
            format = "WAVE"
            subChunk1Id = "fmt "
            subChunk1Size = 16
            audioFormat = 1 // PCM
            this.channels = channels
            this.sampleRate = audioRecord.sampleRate
            this.bitsPerSample = bits
            //byteRate = sampleRate * channels * bits / 8
            blockAlign = (channels * bits / 8).toShort().toInt()
            subChunk2Id = "data"
            subChunk2Size = 0 // This can be updated when actual data is available
        }

        createLog("waveThread-->${waveHeader}-----")
        createLog("waveThread-->${channels}-----")
        createLog("waveThread-->${bits}-----")

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
                val isClap = frameBytes?.let { clapApi.isClap(it) }
                if (isClap == true) numClaps++
                if (isWhistle) numWhistles++
                whistleResultList.add(isWhistle)
                clapResultList.add(isClap == true)
                if (isWhistle) {
                    createLog("isWhistleDetectedSatti-->isWhistle")
                }
                createLog("DetailOfAudio", "$numWhistles sands $numClaps")
                if (numWhistles >= 3) {
                    Log.e("Sound", "Whistle Detected$numWhistles")
                    initBuffer()
                    onSignalsDetectedListener?.onWhistleDetected()
                }

                val clapNumber = numOfClapSensitivity.getSoundSensitivityLevel() ?: 0
                val clapSensitivity = when (clapNumber) {
                    0 -> 3
                    1 -> 2
                    2 -> 1
                    else -> 0
                }
                Log.d("ClapLogicNaju", "numClaps at onClapDetected $numClaps--$clapSensitivity")
                if (numClaps >= clapSensitivity) {
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

