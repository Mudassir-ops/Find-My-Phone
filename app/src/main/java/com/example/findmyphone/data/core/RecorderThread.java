package com.example.findmyphone.data.core;

import android.annotation.SuppressLint;
import android.media.AudioRecord;

public class RecorderThread extends Thread {
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
}
