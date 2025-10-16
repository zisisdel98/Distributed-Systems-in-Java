package com.activitytracker;

import java.io.Serializable;

public class ActivityResult implements Serializable {
    private String uName;
    private double totalDistance;
    private double totalClimb;
    private double totalTime;
    private double averageSpeed;


    public ActivityResult(String name, double totalDistance, double totalClimb, double totalTime) {
        this.uName = name;
        this.totalDistance = totalDistance;
        this.totalClimb = totalClimb;
        this.totalTime = totalTime;
    }

    public ActivityResult(String name)
    {
        uName = name;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public double getTotalClimb() {
        return totalClimb;
    }

    public void setTotalClimb(double totalClimb) {
        this.totalClimb = totalClimb;
    }

    public double getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(double totalTime) {
        this.totalTime = totalTime;
    }
    public double getAverageSpeed() {
        return averageSpeed;
    }
    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }
    @Override
    public String toString() {
        return "ActivityResult{" +
                "user=" + uName +
                "  totalDistance=" +String.format("%.3f", totalDistance / 1000.0) +"km"+
                ", totalClimb=" +String.format("%.3f", totalClimb )+
                ", totalTime=" + (int) (totalTime / 60)+ "min and "+ (int) (totalTime % 60) + "sec"+
                ", averageSpeed=" + String.format("%.3f",averageSpeed )+"km/h"+
                '}';
    }


    public void setUsername(String uName) {
        this.uName = uName;
    }

    public String getUsername() {
        return uName;
    }
}
