package com.example.findmyphone

import android.app.Activity
import android.content.Context
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor

class PitchDetector(private val context: Context) {

    private var dispatcher: AudioDispatcher? = null

    fun startDetection(onPitchDetected: (Float) -> Unit) {
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0)

        val pdh = PitchDetectionHandler { result, _ ->
            val pitchInHz = result.pitch
            (context as? Activity)?.runOnUiThread {
                onPitchDetected(pitchInHz)
            }
        }

        val pitchProcessor = PitchProcessor(
            PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
            22050F,
            1024,
            pdh
        )

        dispatcher?.addAudioProcessor(pitchProcessor)

        Thread(dispatcher, "Audio Thread").start()
    }

    fun stopDetection() {
        dispatcher?.stop()
    }
}
