package com.example.findmyphone.data.core

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import be.tarsos.dsp.io.UniversalAudioInputStream
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.mfcc.MFCC
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.example.findmyphone.TEST.musicg.api.ClapApi
import com.example.findmyphone.TEST.musicg.api.WhistleApi
import com.example.findmyphone.TEST.musicg.wave.WaveHeader
import com.example.findmyphone.utils.Logs
import com.example.findmyphone.utils.Logs.createLog
import com.example.findmyphone.utils.SessionManager
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.LinkedList
import kotlin.concurrent.Volatile
import kotlin.math.sqrt

@SuppressLint("MissingPermission")
class DetectorThread(
    private val recorder: RecorderThread,
    private val numOfClapSensitivity: SessionManager,
    private val context: Context
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
    private val referenceMFCC by lazy {
        extractMFCCFromAsset(context, "clap_sound.wav")
    }

    private fun extractMFCCFromAsset(context: Context, assetFileName: String): List<FloatArray> {
        val bufferSize = 2048
        val overlap = 1024
        val sampleRate = 44100f
        val mfcc = MFCC(bufferSize, sampleRate, 13, 20, 300f, 8000f)
        val mfccList = mutableListOf<FloatArray>()
        val afd = context.assets.openFd(assetFileName)
        val inputStream = BufferedInputStream(afd.createInputStream())
        val dispatcher = AudioDispatcher(
            UniversalAudioInputStream(
                inputStream,
                TarsosDSPAudioFormat(sampleRate, 16, 1, true, false)
            ), bufferSize, overlap
        )
        dispatcher.addAudioProcessor(mfcc)
        dispatcher.addAudioProcessor(object : AudioProcessor {
            override fun process(audioEvent: AudioEvent): Boolean {
                mfccList.add(mfcc.mfcc.clone())
                return true
            }

            override fun processingFinished() {}
        })

        dispatcher.run()
        return mfccList
    }

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

    private var clapCandidateTime: Long = 0L
    private var isClapCandidate = false

    private val CLAP_MIN = 16.0
    private val CLAP_MAX = 20.0
    private val CANCEL_THRESHOLD = 20.0
    private val CLAP_WINDOW_MS = 500L

    private fun Double.checkExactClapRange(onClapDetected: () -> Unit) {
        val distance = this
        val now = System.currentTimeMillis()
        when {
            distance in CLAP_MIN..CLAP_MAX && !isClapCandidate -> {
                isClapCandidate = true
                clapCandidateTime = now
                Log.d("ClapExtension", "🔍 Possible clap started at: $distance")
            }
            isClapCandidate && now - clapCandidateTime <= CLAP_WINDOW_MS && distance in CLAP_MIN..CLAP_MAX -> {
                isClapCandidate = false
                Log.d("ClapExtension", "✅ Clap confirmed at: $distance")
              //  onClapDetected()
            }

            // If distance exceeds threshold in that window, cancel it
            isClapCandidate && now - clapCandidateTime <= CLAP_WINDOW_MS && distance > CANCEL_THRESHOLD -> {
                isClapCandidate = false
                Log.d("ClapExtension", "❌ Clap cancelled due to high distance: $distance")
            }

            // If time window passes, reset candidate
            isClapCandidate && now - clapCandidateTime > CLAP_WINDOW_MS -> {
                isClapCandidate = false
                Log.d("ClapExtension", "⌛ Clap window expired without confirmation.")
            }

            else -> {
                Log.d("ClapExtension", "ℹ️ No valid clap activity: $distance")
            }
        }
    }


    override fun run() {
        initBuffer()
        try {
            while (running) {
                val frameBytes = recorder.frameBytes


                val oldWhistle = whistleResultList.removeFirst()
                val oldClap = clapResultList.removeFirst()

                val frameMFCC = frameBytes?.let { bytes ->
                    extractMFCCFromPcmNew(pcmBytes = bytes)
                }
                if (frameMFCC != null) {
                    val currentTime = System.currentTimeMillis()
                    // Skip detection if within cooldown
                    val distance = compareMFCCs(frameMFCC, referenceMFCC)
                    distance.checkExactClapRange {
                        onSignalsDetectedListener?.onClapDetected()
                        initBuffer()
                    }
                }


//                val isClapPython = frameBytes?.let { bytes ->
//                    val wavBytes = pcmToWavBytes(bytes) // Convert PCM to WAV
//                    val py = Python.getInstance()
//                    val clapDetector = py.getModule("clap_detector").callAttr("ClapDetector", 6000)
//                    val detectedTimes = clapDetector.callAttr("run", PyObject.fromJava(wavBytes))
//
//                    val hasClap = detectedTimes.asList().isNotEmpty()
//                    createLog("isClapPython --> $hasClap, times: ${detectedTimes.asList()}")
//                    hasClap
//                }
//
//
//                createLog("isClapPython-->$isClapPython")
//                if (isClapPython == true) {
//                    initBuffer()
//                    onSignalsDetectedListener?.onClapDetected()
//                }
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
//                if (numClaps >= clapSensitivity) {
//                    initBuffer()
//                    onSignalsDetectedListener?.onClapDetected()
//                }

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

    private fun compareMFCCs(mfcc1: List<FloatArray>, mfcc2: List<FloatArray>): Double {
        val minSize = minOf(mfcc1.size, mfcc2.size)
        var total = 0.0

        for (i in 0 until minSize) {
            val f1 = mfcc1[i]
            val f2 = mfcc2[i]

            val dist = f1.zip(f2).sumOf { (a, b) -> ((a - b) * (a - b)).toDouble() }
            total += sqrt(dist)
        }

        return total / minSize
    }

    fun pcmToWavBytes(pcmData: ByteArray, sampleRate: Int = 44100): ByteArray {
        val byteRate = 16 * sampleRate / 8
        val totalDataLen = pcmData.size + 36
        val channels = 1

        val header = ByteArrayOutputStream().apply {
            write("RIFF".toByteArray())
            write(
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(totalDataLen).array()
            )
            write("WAVE".toByteArray())
            write("fmt ".toByteArray())
            write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(16).array())
            write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(1).array())
            write(
                ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(channels.toShort())
                    .array()
            )
            write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(sampleRate).array())
            write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(byteRate).array())
            write(
                ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((2).toShort())
                    .array()
            )
            write(
                ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(16.toShort()).array()
            )
            write("data".toByteArray())
            write(
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(pcmData.size).array()
            )
            write(pcmData)
        }
        return header.toByteArray()
    }

    private fun createDispatcherFromFloatArray(
        audioData: FloatArray,
        sampleRate: Int,
        bufferSize: Int,
        overlap: Int
    ): AudioDispatcher {
        // Convert float array to byte array (PCM 16-bit little-endian)
        val byteBuffer = ByteBuffer.allocate(audioData.size * 2)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        audioData.forEach { floatSample ->
            val sample = (floatSample * Short.MAX_VALUE).toInt().toShort()
            byteBuffer.putShort(sample)
        }
        val byteArray = byteBuffer.array()
        val bais = ByteArrayInputStream(byteArray)
        val tarsosFormat = TarsosDSPAudioFormat(
            sampleRate.toFloat(), 16, 1, true, false
        )
        val audioStream = UniversalAudioInputStream(bais, tarsosFormat)
        return AudioDispatcher(audioStream, bufferSize, overlap)
    }

    private fun extractMFCCFromPcmNew(pcmBytes: ByteArray): List<FloatArray> {
        val sampleRate = 44100
        val audioBuffer = ByteBuffer.wrap(pcmBytes)
            .order(ByteOrder.LITTLE_ENDIAN)
            .asShortBuffer()
        val audioData = ShortArray(audioBuffer.remaining())
        audioBuffer.get(audioData)

        val floatData = audioData.map { it.toFloat() / Short.MAX_VALUE }.toFloatArray()

        val dispatcher = createDispatcherFromFloatArray(floatData, sampleRate, 2048, 1024)
        val mfccList = mutableListOf<FloatArray>()
        val mfcc = MFCC(2048, sampleRate.toFloat(), 13, 20, 300f, 8000f)

        dispatcher.addAudioProcessor(mfcc)
        dispatcher.addAudioProcessor(object : AudioProcessor {
            override fun process(audioEvent: AudioEvent): Boolean {
                mfccList.add(mfcc.mfcc.clone())
                return true
            }

            override fun processingFinished() {}
        })

        dispatcher.run()
        return mfccList
    }


}

