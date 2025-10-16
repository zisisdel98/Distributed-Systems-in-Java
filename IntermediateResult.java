package com.activitytracker;

import java.io.Serializable;

public class IntermediateResult implements Serializable {
    private int workerId;
    private double partialDistance;
    private double partialClimb;
    private double partialTime;

    public IntermediateResult(double partialDistance, double partialClimb, double partialTime) {
        this.partialDistance = partialDistance;
        this.partialClimb = partialClimb;
        this.partialTime = partialTime;
    }

    public IntermediateResult() {}

    public void setWorkerId(int workerId) {
        this.workerId = workerId;
    }

    public double getPartialDistance() {
        return partialDistance;
    }

    public void setPartialDistance(double partialDistance) {
        this.partialDistance = partialDistance;
    }

    public double getPartialClimb() {
        return partialClimb;
    }

    public void setPartialClimb(double partialClimb) {
        this.partialClimb = partialClimb;
    }

    public double getPartialTime() {
        return partialTime;
    }

    public void setPartialTime(double partialTime) {
        this.partialTime = partialTime;
    }

    @Override
    public String toString() {
        return "IntermediateResult{" +
                ", partialDistance=" + String.format("%.3f", partialDistance / 1000.0) +"km"+
                ", partialClimb=" +String.format("%.3f", partialClimb )+
                ", partialTime=" + (int) (partialTime / 60)+ "min and "+ (int) (partialTime % 60) + "sec"+
                '}';
    }
}

