package com.example.findmyphone

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.chaquo.python.PyObject
import com.chaquo.python.Python

class RealTimeClapDetector(
    private val context: Context,
    private val onClapDetected: () -> Unit
) {
    private var isRecording = false
    private lateinit var audioRecord: AudioRecord
    private val bufferSize = AudioRecord.getMinBufferSize(
        SAMPLE_RATE,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private val py = Python.getInstance()
    private val detector =
        py.getModule("clap_detector").callAttr("ClapDetector", 10000) // bias threshold

    fun start() {
        if (ActivityCompat.checkSelfPermission(
                context,
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
            bufferSize
        )
        audioRecord.startRecording()
        isRecording = true

        Thread {
            val buffer = ByteArray(bufferSize)
            while (isRecording) {
                val read = audioRecord.read(buffer, 0, buffer.size)
                if (read > 0) {
                    val result = detector.callAttr("run", PyObject.fromJava(buffer))
                    val times = result.asList().map { it.toString().toFloat() }
                    if (times.isNotEmpty()) {
                        Log.d("CLAP", "Clap Detected!")
                        onClapDetected()
                        Thread.sleep(500) // debounce
                    }
                }
            }
        }.start()
    }

    fun stop() {
        isRecording = false
        audioRecord.stop()
        audioRecord.release()
    }

    companion object {
        private const val SAMPLE_RATE = 16000
    }
}
