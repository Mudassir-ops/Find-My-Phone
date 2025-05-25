package com.example.findmyphone.data.core

data class AudioFrame(val amplitude: Int, val index: Int)

class ClapDetector(
    private val amplitudeThreshold: Int = 1000,      // must be loud
    private val minSilenceBefore: Int = 5,           // silence before peak
    private val minSilenceAfter: Int = 5,            // silence after peak
    private val minFrameGap: Int = 10                // frames between claps to prevent flooding
) {
    private var lastClapIndex = -minFrameGap

    fun detectClap(frames: List<AudioFrame>): List<AudioFrame> {
        val claps = mutableListOf<AudioFrame>()

        for (i in minSilenceBefore until frames.size - minSilenceAfter) {
            val current = frames[i]
            if (current.amplitude < amplitudeThreshold) continue

            val isBeforeSilent = frames.subList(i - minSilenceBefore, i)
                .all { it.amplitude < amplitudeThreshold / 2 }
            val isAfterSilent = frames.subList(i + 1, i + 1 + minSilenceAfter)
                .all { it.amplitude < amplitudeThreshold / 2 }

            if (isBeforeSilent && isAfterSilent && current.index - lastClapIndex > minFrameGap) {
                claps.add(current)
                lastClapIndex = current.index
            }
        }

        return claps
    }
}
