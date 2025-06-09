package com.example.findmyphone.TEST.musicg.math.statistics;

public abstract class MathStatistics {
    protected double[] values;

    public void setValues(double[] values) {
        this.values = values;
    }

    public abstract double evaluate();
}