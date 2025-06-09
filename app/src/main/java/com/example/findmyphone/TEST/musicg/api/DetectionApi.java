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


import android.util.Pair;

import com.example.findmyphone.TEST.musicg.math.rank.ArrayRankDouble;
import com.example.findmyphone.TEST.musicg.math.statistics.StandardDeviation;
import com.example.findmyphone.TEST.musicg.math.statistics.ZeroCrossingRate;
import com.example.findmyphone.TEST.musicg.wave.Wave;
import com.example.findmyphone.TEST.musicg.wave.WaveHeader;
import com.example.findmyphone.TEST.musicg.wave.extension.Spectrogram;

import java.util.ArrayList;
import java.util.List;

import kotlin.Triple;

/**
 * Api for detecting different sounds
 *
 * @author Jacquet Wong
 */
public class DetectionApi {

    protected WaveHeader waveHeader;
    protected int fftSampleSize;
    protected int numFrequencyUnit;
    protected double unitFrequency;
    protected double minFrequency, maxFrequency;
    protected double minIntensity, maxIntensity;
    protected double minStandardDeviation, maxStandardDeviation;
    protected int highPass, lowPass;
    protected int minNumZeroCross, maxNumZeroCross, energyThreshold;
    protected int lowerBoundary, upperBoundary;
    protected int numRobust;

    public DetectionApi(WaveHeader waveHeader) {
        if (waveHeader.getChannels() == 1) {
            this.waveHeader = waveHeader;
            init();
        } else {
            System.err.println("DetectionAPI supports mono Wav only");
        }
    }

    /**
     * Initiate the settings for specific sound detection
     */
    protected void init() {
        // do nothing, needed to be overrided
    }

    /**
     * Determine the audio bytes contains a specific sound or not
     *
     * @param audioBytes input audio byte
     * @return
     */
    public boolean isSpecificSound(byte[] audioBytes) {
        int bytesPerSample = this.waveHeader.getBitsPerSample() / 8;
        int numSamples = audioBytes.length / bytesPerSample;
        if (numSamples > 0 && Integer.bitCount(numSamples) == 1) {
            this.fftSampleSize = numSamples;
            this.numFrequencyUnit = this.fftSampleSize / 2;
            this.unitFrequency = (double) this.waveHeader.getSampleRate() / 2.0 / (double) this.numFrequencyUnit;
            this.lowerBoundary = (int) ((double) this.highPass / this.unitFrequency);
            this.upperBoundary = (int) ((double) this.lowPass / this.unitFrequency);
            Wave wave = new Wave(this.waveHeader, audioBytes);
            short[] amplitudes = wave.getSampleAmplitudes();
            Spectrogram spectrogram = wave.getSpectrogram(this.fftSampleSize, 0);
            double[][] spectrogramData = spectrogram.getAbsoluteSpectrogramData();
            double[] spectrum = spectrogramData[0];
            int frequencyUnitRange = this.upperBoundary - this.lowerBoundary + 1;
            double[] rangedSpectrum = new double[frequencyUnitRange];
            System.arraycopy(spectrum, this.lowerBoundary, rangedSpectrum, 0, rangedSpectrum.length);
            if (frequencyUnitRange <= spectrum.length) {
                return this.isPassedIntensity(spectrum) && this.isPassedStandardDeviation(spectrogramData) && this.isPassedZeroCrossingRate(amplitudes) && this.isPassedFrequency(rangedSpectrum);
            } else {
                System.err.println("is error: the wave needed to be higher sample rate");
            }
        } else {
            System.out.println("The sample size must be a power of 2");
        }

        return false;
    }


    public boolean isClapDetected(
            byte[] audioBytes
    ) {
        int bytesPerSample = waveHeader.getBitsPerSample() / 8;
        int numSamples = audioBytes.length / bytesPerSample;

        // numSamples required to be a power of 2
        if (numSamples <= 0 || Integer.bitCount(numSamples) != 1) {
            System.out.println("The sample size must be a power of 2");
            return false;
        }

        fftSampleSize = numSamples;
        numFrequencyUnit = fftSampleSize / 2;

        // frequency could be caught within the half of nSamples according to Nyquist theory
        unitFrequency = (double) waveHeader.getSampleRate() / 2 / numFrequencyUnit;

        // set boundary
        lowerBoundary = (int) (highPass / unitFrequency);
        upperBoundary = (int) (lowPass / unitFrequency);
        // end set boundary

        Wave wave = new Wave(waveHeader, audioBytes);    // audio bytes of this frame
        short[] amplitudes = wave.getSampleAmplitudes();

        // spectrum for the clip
        Spectrogram spectrogram = wave.getSpectrogram(fftSampleSize, 0);

        double[][] spectrogramData = spectrogram.getAbsoluteSpectrogramData();

        // since fftSampleSize==numSamples, there're only one spectrum which is thisFrameSpectrogramData[0]
        double[] spectrum = spectrogramData[0];

        int frequencyUnitRange = upperBoundary - lowerBoundary + 1;
        if (spectrum.length < frequencyUnitRange) {
            System.err.println("is error: the wave needed to be higher sample rate");
            return false;
        }

        double[] rangedSpectrum = new double[frequencyUnitRange];
        System.arraycopy(spectrum, lowerBoundary, rangedSpectrum, 0, rangedSpectrum.length);


        // 1. ZCR + Energy check
        Pair<Integer, Integer> zcrResult = isPassedZeroCrossingRateClap(amplitudes);
        boolean zcrEnergyPassed = (zcrResult.first >= (int) (lowZcrRatioForClap * zcrHistorySize));

        // 2. Frequency range check
        Pair<Boolean, Double> frequencyResult = isPassedFrequencyClap(spectrum);
        boolean frequencyPassed = frequencyResult.first;
        double freq = frequencyResult.second;

        // 3. Average intensity check
        Pair<Boolean, Double> intensityResult = isPassedIntensityClap(spectrum);
        boolean intensityPassed = intensityResult.first;
        double intensityFreq = intensityResult.second;


        // 4. Standard deviation of top frequencies
        Pair<Boolean, Double> stdDevResult = isPassedStandardDeviationClap(spectrogramData);
        boolean stdDevPassed = stdDevResult.first;
        double std = stdDevResult.second;


        // 5. Final decision: only detect clap if all conditions pass
        boolean isClap = zcrEnergyPassed && frequencyPassed && intensityPassed && stdDevPassed;

        // Debug logging for tuning
        System.out.println("----- Clap Detection Debug -----");
        System.out.println("ZCR Low Count: " + zcrResult.first + "/" + zcrResult.second);
        System.out.println("Passed Frequency: " + frequencyPassed);
        System.out.println("Passed Intensity: " + intensityPassed);
        System.out.println("Passed SD: " + stdDevPassed);
        System.out.println("Final Clap Detected: " + isClap);
        System.out.println("--------------------------------");
        if (isClap) {
            System.out.println("Passed FrequencyAndr: " + frequencyPassed + "freq: " + freq);
            System.out.println("Passed FrequencyAndr: " + intensityPassed + "intensityPassed: " + intensityFreq);
            System.out.println("Passed FrequencyAndr: " + stdDevPassed + "intensityPassed: " + std);
        }
        return isClap;
    }

    protected void normalizeSpectrogramData(double[][] spectrogramData) {
        // normalization of absoultSpectrogram
        // set max and min amplitudes
        double maxAmp = Double.MIN_VALUE;
        double minAmp = Double.MAX_VALUE;
        for (double[] spectrogramDatum : spectrogramData) {
            for (int j = 0; j < spectrogramDatum.length; j++) {
                if (spectrogramDatum[j] > maxAmp) {
                    maxAmp = spectrogramDatum[j];
                } else if (spectrogramDatum[j] < minAmp) {
                    minAmp = spectrogramDatum[j];
                }
            }
        }
        // end set max and min amplitudes

        // normalization
        // avoiding divided by zero
        double minValidAmp = 0.00000000001F;
        if (minAmp == 0) {
            minAmp = minValidAmp;
        }
        double diff = Math.log10(maxAmp / minAmp); // perceptual difference
        for (int i = 0; i < spectrogramData.length; i++) {
            for (int j = 0; j < spectrogramData[i].length; j++) {
                if (spectrogramData[i][j] < minValidAmp) {
                    spectrogramData[i][j] = 0;
                } else {
                    spectrogramData[i][j] = (Math.log10(spectrogramData[i][j] / minAmp)) / diff;
                }
            }
        }
        // end normalization
    }

    protected boolean isPassedStandardDeviation(double[][] spectrogramData) {

        // normalize the spectrogramData (with all frames in the spectrogram)
        normalizeSpectrogramData(spectrogramData);

        // analyst data in this frame
        // since fftSampleSize==numSamples, there're only one spectrum which is spectrogramData[last]
        double[] spectrum = spectrogramData[spectrogramData.length - 1];
        // find top most robust frequencies in this frame
        double[] robustFrequencies = new double[numRobust];
        ArrayRankDouble arrayRankDouble = new ArrayRankDouble();
        double nthValue = arrayRankDouble.getNthOrderedValue(spectrum, numRobust, false);
        // end analyst data in this frame

        int count = 0;
        for (double v : spectrum) {
            if (v >= nthValue) {
                robustFrequencies[count++] = v;
                if (count >= numRobust) {
                    break;
                }
            }
        }
        // end find top most robust frequencies

        StandardDeviation standardDeviation = new StandardDeviation();
        standardDeviation.setValues(robustFrequencies);
        double sd = standardDeviation.evaluate();

        // range of standard deviation
        //System.out.println("sd: " + sd + " " + result);
        return (sd >= minStandardDeviation && sd <= maxStandardDeviation);
    }


    protected Pair<Boolean, Double> isPassedStandardDeviationClap(double[][] spectrogramData) {

        // normalize the spectrogramData (with all frames in the spectrogram)
        normalizeSpectrogramData(spectrogramData);

        // analyst data in this frame
        // since fftSampleSize==numSamples, there're only one spectrum which is spectrogramData[last]
        double[] spectrum = spectrogramData[spectrogramData.length - 1];
        // find top most robust frequencies in this frame
        double[] robustFrequencies = new double[numRobust];
        ArrayRankDouble arrayRankDouble = new ArrayRankDouble();
        double nthValue = arrayRankDouble.getNthOrderedValue(spectrum, numRobust, false);
        // end analyst data in this frame

        int count = 0;
        for (double v : spectrum) {
            if (v >= nthValue) {
                robustFrequencies[count++] = v;
                if (count >= numRobust) {
                    break;
                }
            }
        }
        // end find top most robust frequencies

        StandardDeviation standardDeviation = new StandardDeviation();
        standardDeviation.setValues(robustFrequencies);
        double sd = standardDeviation.evaluate();
        boolean result = sd >= this.minStandardDeviation && sd <= this.maxStandardDeviation;
        // range of standard deviation
        System.out.println("sd: " + sd + " " + result);
        return new Pair<>(result, sd);
    }


    protected boolean isPassedFrequency(double[] spectrum) {
        // find the robust frequency
        ArrayRankDouble arrayRankDouble = new ArrayRankDouble();
        double robustFrequency = arrayRankDouble.getMaxValueIndex(spectrum) * unitFrequency;

        // frequency of the sound should not be too low or too high
        boolean result = (robustFrequency >= minFrequency && robustFrequency <= maxFrequency);
        //System.out.println("freq: " + robustFrequency + " " + result);
        return result;
    }

    protected Pair<Boolean, Double> isPassedFrequencyClap(double[] spectrum) {
        // find the robust frequency
        ArrayRankDouble arrayRankDouble = new ArrayRankDouble();
        double robustFrequency = arrayRankDouble.getMaxValueIndex(spectrum) * unitFrequency;

        // frequency of the sound should not be too low or too high
        boolean result = (robustFrequency >= minFrequency && robustFrequency <= maxFrequency);
        System.out.println("isPassedFrequencyClap: " + robustFrequency + " " + result);


        return new Pair<>(result, robustFrequency);
    }

    protected boolean isPassedIntensity(double[] spectrum) {
        // get the average intensity of the signal
        double intensity = 0;
        for (double v : spectrum) {
            intensity += v;
        }
        intensity /= spectrum.length;
        // end get the average intensity of the signal

        // intensity of the whistle should not be too soft
        boolean result = (intensity > minIntensity && intensity <= maxIntensity);
        System.out.println("intensitySatti: " + intensity + " " + result);
        return result;
    }

    protected Pair<Boolean, Double> isPassedIntensityClap(double[] spectrum) {
        // get the average intensity of the signal
        double intensity = 0;
        for (double v : spectrum) {
            intensity += v;
        }
        intensity /= spectrum.length;
        // end get the average intensity of the signal

        boolean result = (intensity > minIntensity && intensity <= maxIntensity);
        System.out.println("intensitySattiClap: " + intensity + "+ " + minIntensity + "minClap" + result + "" + maxIntensity);
        return new Pair<>(result, intensity);
    }

    protected boolean isPassedZeroCrossingRate(short[] amplitudes) {
        ZeroCrossingRate zcr = new ZeroCrossingRate(amplitudes, 1);
        int numZeroCrosses = (int) zcr.evaluate();

        // different sound has different range of zero crossing value
        // when lengthInSecond=1, zero crossing rate is the num
        // of zero crosses
        boolean result = (numZeroCrosses >= minNumZeroCross && numZeroCrosses <= maxNumZeroCross);
        System.out.println("zcrSatti: " + numZeroCrosses + " " + result);

        return result;
    }

    private final List<Integer> zcrHistory = new ArrayList<>();

    private final int zcrHistorySize = 6; // Size of sliding window (about 2 seconds with 3 samples/sec)
    private final int zcrMaxForSpeechReject = 100; // ZCR max threshold for rejecting speech (can be adjusted)
    // The minimum proportion of frames that need to be low ZCR to be considered a clap (e.g., 2/3)
    private final double lowZcrRatioForClap = 2.0 / 3.0;

    protected Pair<Integer, Integer> isPassedZeroCrossingRateClap(short[] amplitudes) {
        // Step 1: Calculate ZCR (Zero Crossing Rate)
        ZeroCrossingRate zcr = new ZeroCrossingRate(amplitudes, 0.5);
        int numZeroCrosses = (int) zcr.evaluate();

        // Step 2: Calculate RMS Energy
        double energy = calculateRmsEnergy(amplitudes);

        // Maintain sliding window of ZCR values
        if (zcrHistory.size() >= zcrHistorySize) {
            zcrHistory.remove(0);
        }
        zcrHistory.add(numZeroCrosses);

        // Step 3: Check if enough frames have low ZCR (2/3 or more of the history)
        int lowZcrCount = 0;
        for (int zcrVal : zcrHistory) {
            if (zcrVal <= zcrMaxForSpeechReject) {
                lowZcrCount++;
            }
        }

        // Calculate if enough frames have low ZCR (>= 2/3)
        boolean recentZcrLow = (lowZcrCount >= lowZcrRatioForClap * zcrHistorySize);

        // Step 4: Check if ZCR and energy criteria are met for clap
        boolean zcrPassed = numZeroCrosses >= minNumZeroCross && numZeroCrosses <= maxNumZeroCross;
        boolean energyPassed = energy > energyThreshold;

        // Final clap detection decision
        boolean isClap = zcrPassed && energyPassed && recentZcrLow;


        System.out.println("zcrSattiClap: " + numZeroCrosses + ", energy: " + energy + ", recentZcrLow: " + recentZcrLow + " (frames with low ZCR: " + lowZcrCount + "/" + zcrHistorySize + "), result: " + isClap);

        return new Pair<>(lowZcrCount, zcrHistorySize);

    }

    private double calculateRmsEnergy(short[] amplitudes) {
        double sum = 0;
        for (short amp : amplitudes) {
            sum += amp * amp;
        }
        return Math.sqrt(sum / amplitudes.length);
    }

}

