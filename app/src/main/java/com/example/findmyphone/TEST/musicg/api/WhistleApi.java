/*
 * Copyright (C) 2011 Jacquet Wong
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

package com.example.findmyphone.TEST.musicg.api;

import com.example.findmyphone.TEST.musicg.wave.WaveHeader;


/**
 * Api for detecting whistle
 *
 * @author Jacquet Wong
 */
public class WhistleApi extends DetectionApi {
    public WhistleApi(WaveHeader waveHeader) {
        super(waveHeader);
    }

    protected void init() {
        this.minFrequency = 600.0;
        this.maxFrequency = Double.MAX_VALUE;
        this.minIntensity = 100.0;
        this.maxIntensity = 100000.0;
        this.minStandardDeviation = 0.10000000149011612;
        this.maxStandardDeviation = 1.0;
        this.highPass = 100;
        this.lowPass = 10000;
        this.minNumZeroCross = 50;
        this.maxNumZeroCross = 200;
        this.numRobust = 10;
    }

    public boolean isWhistle(byte[] audioBytes) {
        return this.isSpecificSound(audioBytes);
    }
}