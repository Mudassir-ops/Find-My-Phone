package com.example.findmyphone

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.chaquo.python.Python
import com.example.findmyphone.utils.Logs

class AudioAnalyzer(val py: Python) {

    private val bufferSize1 = AudioRecord.getMinBufferSize(
        44100,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    @SuppressLint("MissingPermission")
    private val recorder = AudioRecord(
        MediaRecorder.AudioSource.MIC,
        44100,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
        bufferSize1
    )

    private val pyObj = py.getModule("clap_detector")

    fun startRecording() {
        recorder.startRecording()

        Thread {
            val buffer = ShortArray(bufferSize1)

            while (true) {
                val read = recorder.read(buffer, 0, buffer.size)
                if (read > 0) {
                    Logs.createLog("pyhtonCode-->$read")
                    val floatArray = FloatArray(read) { i -> buffer[i] / 32768f }
                    Logs.createLog("floatArray-->$floatArray")
                    val result = pyObj.callAttr("detect_clap", floatArray)
                    //  Logs.createLog("pyhtonCode-->$result")
//                    // Convert to float list for Python
//                    val floatList = buffer.take(read).map { it / 32768f } // Normalize to [-1, 1]
//                    val result = pyObj.callAttr("detect_clap", floatList)
                    val detection = result?.toString()
                    Logs.createLog("PythonResultDetection-->$detection")
                    detection?.let {
                        Logs.createLog("PythonResult-->$it")
                        Log.d("PythonResult", it)
                    }
                }
                Thread.sleep(100)
            }
        }.start()
    }

    fun stopRecording() {
        recorder.stop()
        recorder.release()
    }
}
