package com.example.findmyphone.data.core;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class RecorderThread extends Thread {
    private final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private final AudioRecord audioRecord;
    private final int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    private final int frameByteSize = 2048;  // Size of each audio frame
    private boolean isRecording = false;
    private int sampleRate = 44100;  // Standard sample rate for audio capture
    private final byte[] buffer;

    @SuppressLint("MissingPermission")
    public RecorderThread() {
        this.audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC, // Microphone as input source
                sampleRate,
                channelConfiguration,
                audioEncoding,
                AudioRecord.getMinBufferSize(sampleRate, channelConfiguration, audioEncoding)
        );
        this.buffer = new byte[frameByteSize];  // Buffer to hold the audio data for each frame
    }

    public AudioRecord getAudioRecord() {
        return this.audioRecord;
    }

    public void startRecording() {
        try {
            this.audioRecord.startRecording();
            this.isRecording = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        try {
            this.audioRecord.stop();
            this.audioRecord.release();
            this.isRecording = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // This method reads audio data into a buffer and returns it if it's above a threshold
    public byte[] getFrameBytes() {
        int i;
        int i2 = 0;
        this.audioRecord.read(this.buffer, 0, this.frameByteSize);  // Read data into the buffer
        int i3 = 0;

        // Accumulate the absolute values of the audio data (16-bit PCM format)
        while (true) {
            i = this.frameByteSize;
            if (i2 >= i) {
                break;
            }
            byte[] bArr = this.buffer;
            i3 += Math.abs((int) ((short) ((bArr[i2 + 1] << 8) | bArr[i2])));
            i2 += 2;  // Move by 2 bytes because we're processing 16-bit data (2 bytes per sample)
        }

        // Simple thresholding for detecting significant sound (clap)
        // Here, we require a minimum accumulated sound value to return the buffer
        if (((float) ((i3 / i) / 2)) < 50.0f) {  // Adjusted threshold for clap sensitivity
            return null;  // If sound is below the threshold, return null
        }

        return this.buffer;  // Return the frame buffer for further processing
    }

    @Override
    public void run() {
        startRecording();  // Start the recording when the thread runs
    }

    @Override
    public void interrupt() {
        super.interrupt();  // Interrupt the thread if needed
    }
}
