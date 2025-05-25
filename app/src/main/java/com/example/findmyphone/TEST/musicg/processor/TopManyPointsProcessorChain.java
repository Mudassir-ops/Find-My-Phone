package com.example.findmyphone.TEST.musicg.processor;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TopManyPointsProcessorChain {

    private double[][] intensities;
    List<IntensityProcessor> processorList = new LinkedList<IntensityProcessor>();

    public TopManyPointsProcessorChain(double[][] intensities, int numPoints) {
        this.intensities = intensities;
        RobustIntensityProcessor robustProcessor = new RobustIntensityProcessor(intensities, numPoints);
        processorList.add(robustProcessor);
        process();
    }

    private void process() {
        for (IntensityProcessor processor : processorList) {
            processor.execute();
            intensities = processor.getIntensities();
        }
    }

    public double[][] getIntensities() {
        return intensities;
    }
}