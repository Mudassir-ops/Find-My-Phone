package com.example.findmyphone.data.core;

import android.annotation.SuppressLint;
import android.media.AudioRecord;

import java.util.LinkedList;

public class RecorderClapThread extends Thread {
    private int audioEncoding = 2;
    private AudioRecord audioRecord;
    byte[] buffer;
    private int channelConfiguration = 16;
    private int frameByteSize = 2048;
    private int sampleRate = 44100;

    // Sensitivity tuning parameters
    private static final int MIN_CLAP_AMPLITUDE = 6000; // Minimum amplitude to consider a potential clap
    private static final double AMPLITUDE_SPIKE_RATIO = 1.5; // Ratio of spike compared to the average sound level

    private LinkedList<Double> previousAmplitudes = new LinkedList<>();

    @SuppressLint("MissingPermission")
    public RecorderClapThread() {
        this.audioRecord = new AudioRecord(1, sampleRate, channelConfiguration, audioEncoding, AudioRecord.getMinBufferSize(sampleRate, channelConfiguration, audioEncoding));
        this.buffer = new byte[frameByteSize];
    }

    public AudioRecord getAudioRecord() {
        return this.audioRecord;
    }

    public void startRecording() {
        try {
            this.audioRecord.startRecording();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        try {
            this.audioRecord.stop();
            this.audioRecord.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] getFrameBytes() {
        int i;
        int i2 = 0;
        this.audioRecord.read(this.buffer, 0, this.frameByteSize);
        int i3 = 0;
        while (true) {
            i = this.frameByteSize;
            if (i2 >= i) {
                break;
            }
            byte[] bArr = this.buffer;
            i3 += Math.abs((short) ((bArr[i2 + 1] << 8) | bArr[i2]));
            i2 += 2;
        }

        // Detect if the amplitude exceeds the minimum threshold for a possible clap
        if (((float) ((i3 / i) / 2)) < MIN_CLAP_AMPLITUDE) {
            return null;  // Not enough sound to be considered a clap
        }

        return this.buffer;
    }



    public void run() {
        startRecording();
    }

    public void interrupt() {
        super.interrupt();
    }
}

/*public class RecorderThread extends Thread {
    private int audioEncoding = 2;
    private AudioRecord audioRecord;
    byte[] buffer;
    private int channelConfiguration = 16;
    private int frameByteSize = 2048;
    private int sampleRate = 44100;

    @SuppressLint("MissingPermission")
    public RecorderThread() {
        int i = this.sampleRate;
        int i2 = this.channelConfiguration;
        int i3 = this.audioEncoding;
        this.audioRecord = new AudioRecord(1, i, i2, i3, AudioRecord.getMinBufferSize(i, i2, i3));
        this.buffer = new byte[this.frameByteSize];
    }

    public AudioRecord getAudioRecord() {
        return this.audioRecord;
    }

    public void startRecording() {
        try {
            this.audioRecord.startRecording();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        try {
            this.audioRecord.stop();
            this.audioRecord.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] getFrameBytes() {
        int i;
        int i2 = 0;
        this.audioRecord.read(this.buffer, 0, this.frameByteSize);
        int i3 = 0;
        while (true) {
            i = this.frameByteSize;
            if (i2 >= i) {
                break;
            }
            byte[] bArr = this.buffer;
            i3 += Math.abs((short) ((bArr[i2 + 1] << 8) | bArr[i2]));
            i2 += 2;
        }
        if (((float) ((i3 / i) / 2)) < 30.0f) {
            return null;
        }
        return this.buffer;
    }

    public void run() {
        startRecording();
    }

    public void interrupt() {
        super.interrupt();
    }
}*/
