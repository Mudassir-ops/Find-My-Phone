package com.example.findmyphone

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import be.tarsos.dsp.io.UniversalAudioInputStream
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.mfcc.MFCC
import com.example.findmyphone.utils.Logs
import com.example.findmyphone.utils.SessionManager
import com.findmyphone.clapping.clapfinder.soundalert.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

@AndroidEntryPoint
class MainActivity2 : AppCompatActivity() {

    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var clapDetectionJob: Job? = null

    // Settings (matching Python script)
    private val INITIAL_THRESHOLD = 0.5f
    private val NOISE_FLOOR = 0.02f
    private val DEBOUNCE_TIME = 0.2 // Seconds
    private var currentThreshold = INITIAL_THRESHOLD
    private var lastDetection = 0.0
    private var clapCount = 0
    private var mediaPlayer: MediaPlayer? = null
    var clapDetector: ClapDetector? = null

    private val micFileName = "mic_temp.wav"
    private val dogAssetFile = "dog_bark.wav"

    @Inject
    lateinit var sessionManager: SessionManager
    override fun onStart() {
        super.onStart()

    }

    private val TAG = "MFCC"
    private val RECORD_DURATION = 3 // seconds
    private val SAMPLE_RATE = 44100
    private val BUFFER_SIZE = 2048
    private val OVERLAP = 1024
    private lateinit var dogMFCC: List<FloatArray>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        findViewById<Button>(R.id.tv_button1).setOnClickListener {
            // val clapFile = copyAssetToCache(this, "clap_filesss.wav", "clap_Satti.wav")

            val referenceMFCC = extractMFCCFromAsset(this, "clap_filesss.wav")
            extractMFCCFromMicrophone { micMFCC ->
                val distance = compareMFCCs(micMFCC, referenceMFCC)
                Log.d("MFCC", "Distance: $distance")
                when {
                    distance < 35 -> {
                        Log.d("MFCC", "✅ High confidence: It's a dog bark! ($distance)")
                    }

                    distance in 35.0..50.0 -> {
                        Log.d("MFCC", "🟡 Medium confidence: Possibly a dog bark. ($distance)")
                    }

                    else -> {
                        Log.d("MFCC", "❌ Not a dog bark. ($distance)")
                    }
                }
            }
        }


//                    val dogFilePath = copyAssetToInternal(this, dogAssetFile)
//                    val micFilePath = File(filesDir, micFileName).absolutePath
//
//                    recordAudioToWav(this, micFileName, 3)
//
//                    val py = Python.getInstance()
//                    val module = py.getModule("mfcc_comparator")
//                    val result = module.callAttr("compare_mfcc", dogFilePath, micFilePath, 200)
//                    val isSimilar = result.toBoolean()
//                    Log.d("AUDIO", "Dog sound comparison result: $isSimilar")

//        findViewById<Button>(R.id.tv_button).setOnClickListener {
//            val wavBytes = readWavBytesFromAssets(this, "clap.wav")
//            val py = Python.getInstance()
//            val detector = py.getModule("clap_detector").callAttr("ClapDetector", 6000)
//            val result = detector.callAttr("run", PyObject.fromJava(wavBytes))
//            val times = result.asList().map { it.toString().toFloat() }
//            Log.d("CLAPS", "Detected at times: $times")
//        }

//        findViewById<Button>(R.id.tv_button1).setOnClickListener {
//            val file1Path = copyAssetToInternal(this, "clap.wav")
//            val file2Path = copyAssetToInternal(this, "clap.wav")
//            val py = Python.getInstance()
//            val module = py.getModule("mfcc_comparator")
//            val result = module.callAttr("compare_mfcc", file1Path, file2Path)
//            val isSimilar = result.toBoolean()
//            Log.d("AUDIO", "Audio comparison result: $isSimilar")
//        }
    }

    private fun copyAssetToCache(
        context: Context,
        assetFileName: String,
        outputFileName: String
    ): File {
        val file = File(context.cacheDir, outputFileName)
        if (!file.exists()) {
            context.assets.open(assetFileName).use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
        return file
    }

    private fun copyAssetToInternal(context: Context, assetFileName: String): String {
        val file = File(context.filesDir, assetFileName)
        if (!file.exists()) {
            context.assets.open(assetFileName).use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return file.absolutePath
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startClapDetection()
            } else {
                Toast.makeText(this, "Microphone permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun readWavBytesFromAssets(context: Context, fileName: String): ByteArray {
        context.assets.open(fileName).use { inputStream ->
            return inputStream.readBytes()
        }
    }


    private fun startClapDetection() {
        // Initialize AudioRecord
        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            max(bufferSize, BUFFER_SIZE)
        )

        // Start recording and processing in a coroutine
        clapDetectionJob = CoroutineScope(Dispatchers.IO).launch {
            val buffer = ShortArray(BUFFER_SIZE)
            isRecording = true
            audioRecord?.startRecording()

            while (isRecording) {
                val read = audioRecord?.read(buffer, 0, BUFFER_SIZE) ?: 0
                if (read > 0) {
                    // Calculate RMS and peak
                    var sum = 0.0
                    var peak = 0.0f
                    for (i in 0 until read) {
                        val sample = buffer[i].toFloat() / Short.MAX_VALUE // Normalize to [-1, 1]
                        sum += (sample * sample).toDouble()
                        peak = max(peak, abs(sample))
                    }
                    val rms = sqrt(sum / read).toFloat()
                    // Dynamic threshold adjustment
                    currentThreshold = if (rms < NOISE_FLOOR) {
                        INITIAL_THRESHOLD
                    } else {
                        max(INITIAL_THRESHOLD, rms * 1.5f)
                    }

                    // Clap detection logic
                    val currentTime = System.currentTimeMillis() / 1000.0
                    if (peak > currentThreshold &&
                        rms > currentThreshold / 2 &&
                        (currentTime - lastDetection) > DEBOUNCE_TIME
                    ) {
                        lastDetection = currentTime
                        clapCount++
                        Logs.createLog(
                            "👏 Clap #$clapCount detected! (Peak: ${"%.2f".format(peak)}, RMS: ${
                                "%.2f".format(rms)
                            }, Thr: ${"%.2f".format(currentThreshold)})"
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        clapDetectionJob?.cancel()
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
                lifecycleScope.launch {
                    delay(3000)
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


//    private fun recordAudioToWav(context: Context, outputFile: String, duration: Int = 5) {
//        val sampleRate = 44100
//        val channelConfig = AudioFormat.CHANNEL_IN_MONO
//        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
//        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
//        if (ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.RECORD_AUDIO
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            val audioRecord = AudioRecord(
//                MediaRecorder.AudioSource.MIC,
//                sampleRate,
//                channelConfig,
//                audioFormat,
//                bufferSize
//            )
//
//            val buffer = ShortArray(bufferSize / 2)
//            audioRecord.startRecording()
//
//            val output = FileOutputStream(File(context.filesDir, outputFile))
//            val wavHeader = createWavHeader(sampleRate, 16, 1, duration * sampleRate * 2)
//            output.write(wavHeader)
//
//            val startTime = System.currentTimeMillis()
//            while (System.currentTimeMillis() - startTime < duration * 1000) {
//                val read = audioRecord.read(buffer, 0, buffer.size)
//                if (read > 0) {
//                    val bytes = ByteArray(read * 2)
//                    for (i in 0 until read) {
//                        bytes[i * 2] = (buffer[i].toInt() and 0xFF).toByte()
//                        bytes[i * 2 + 1] = ((buffer[i].toInt() shr 8) and 0xFF).toByte()
//                    }
//                    output.write(bytes)
//                }
//            }
//
//            audioRecord.stop()
//            audioRecord.release()
//            output.close()
//        }
//    }
//
//    private fun createWavHeader(
//        sampleRate: Int,
//        bitsPerSample: Int,
//        channels: Int,
//        dataSize: Int
//    ): ByteArray {
//        val header = ByteArray(44)
//        val totalSize = dataSize + 36
//        val byteRate = sampleRate * channels * bitsPerSample / 8
//
//        header[0] = 'R'.toByte(); header[1] = 'I'.toByte(); header[2] = 'F'.toByte(); header[3] =
//            'F'.toByte()
//        header[4] = (totalSize and 0xFF).toByte(); header[5] = ((totalSize shr 8) and 0xFF).toByte()
//        header[6] = ((totalSize shr 16) and 0xFF).toByte(); header[7] =
//            ((totalSize shr 24) and 0xFF).toByte()
//        header[8] = 'W'.toByte(); header[9] = 'A'.toByte(); header[10] = 'V'.toByte(); header[11] =
//            'E'.toByte()
//        header[12] = 'f'.toByte(); header[13] = 'm'.toByte(); header[14] =
//            't'.toByte(); header[15] = ' '.toByte()
//        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0 // Subchunk1Size
//        header[20] = 1; header[21] = 0 // AudioFormat (PCM)
//        header[22] = channels.toByte(); header[23] = 0 // NumChannels
//        header[24] = (sampleRate and 0xFF).toByte(); header[25] =
//            ((sampleRate shr 8) and 0xFF).toByte()
//        header[26] = ((sampleRate shr 16) and 0xFF).toByte(); header[27] =
//            ((sampleRate shr 24) and 0xFF).toByte()
//        header[28] = (byteRate and 0xFF).toByte(); header[29] = ((byteRate shr 8) and 0xFF).toByte()
//        header[30] = ((byteRate shr 16) and 0xFF).toByte(); header[31] =
//            ((byteRate shr 24) and 0xFF).toByte()
//        header[32] = (channels * bitsPerSample / 8).toByte(); header[33] = 0 // BlockAlign
//        header[34] = bitsPerSample.toByte(); header[35] = 0 // BitsPerSample
//        header[36] = 'd'.toByte(); header[37] = 'a'.toByte(); header[38] =
//            't'.toByte(); header[39] = 'a'.toByte()
//        header[40] = (dataSize and 0xFF).toByte(); header[41] = ((dataSize shr 8) and 0xFF).toByte()
//        header[42] = ((dataSize shr 16) and 0xFF).toByte(); header[43] =
//            ((dataSize shr 24) and 0xFF).toByte()
//        return header
//    }

    fun extractMFCCFromAsset(context: Context, assetFileName: String): List<FloatArray> {
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

    private fun extractMFCCFromMicrophone(callback: (List<FloatArray>) -> Unit) {
        val sampleRate = 44100
        val bufferSize = 2048
        val overlap = 1024
        val mfcc = MFCC(bufferSize, sampleRate.toFloat(), 13, 20, 300f, 8000f)
        val mfccList = mutableListOf<FloatArray>()

        val dispatcher =
            AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, bufferSize, overlap)
        dispatcher.addAudioProcessor(mfcc)

        dispatcher.addAudioProcessor(object : AudioProcessor {
            var frameCount = 0
            override fun process(audioEvent: AudioEvent): Boolean {
                mfccList.add(mfcc.mfcc.clone())
                frameCount++
                // Stop after 2 seconds of frames
                if (frameCount > (sampleRate / overlap) * 2) dispatcher.stop()
                return true
            }

            override fun processingFinished() {
                callback(mfccList)
            }
        })

        Thread { dispatcher.run() }.start()
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


}



