/*
 * Copyright (C) 2012 Jacquet Wong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.findmyphone.TEST.musicg.main.demo;


import com.example.findmyphone.TEST.musicg.wave.Wave;
import com.example.findmyphone.TEST.musicg.wave.WaveTypeDetector;

import org.junit.Test;

public class WhistleApiTest {
    @Test
    public void testWhistleDetection() {
        String filename = "/Users/mudassirsatti/AndroidStudioProjects/FindMyPhone/audio_work/clap_sound.wav";
        Wave wave = new Wave(filename);
        WaveTypeDetector waveTypeDetector = new WaveTypeDetector(wave);
        System.out.println("Whistle probability: " + waveTypeDetector.getWhistleProbability());
        //System.out.println("Whistle probability: " + waveTypeDetector.getClapProbability());
    }
}