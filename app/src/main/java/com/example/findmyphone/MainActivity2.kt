package com.example.findmyphone

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chaquo.python.Python
import com.example.findmyphone.utils.Logs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

class MainActivity2 : AppCompatActivity() {

    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var clapDetectionJob: Job? = null

    // Settings (matching Python script)
    private val INITIAL_THRESHOLD = 0.5f
    private val NOISE_FLOOR = 0.02f
    private val DEBOUNCE_TIME = 0.2 // Seconds
    private val SAMPLE_RATE = 44100
    private val BUFFER_SIZE = 1024
    private var currentThreshold = INITIAL_THRESHOLD
    private var lastDetection = 0.0
    private var clapCount = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        val python = Python.getInstance()
        val analyzer = AudioAnalyzer(python)
        analyzer.startRecording()

//        val module = python.getModule("clap_detector")
//
//        val result = module.callAttr("calc_mean")
      //  Log.d("PythonResult", "Mean: $result")

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
}