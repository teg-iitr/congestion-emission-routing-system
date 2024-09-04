package com.map.app;

public class RouteResponse {
    private double distance;
    private double time;
    private double concentration;
    private double exposure;

    public RouteResponse(double distance, double time, double concentration, double exposure) {
        this.distance = distance;
        this.time = time;
        this.concentration = concentration;
        this.exposure = exposure;
    }

    // Getters and setters
    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getConcentration() {
        return concentration;
    }

    public void setConcentration(double concentration) {
        this.concentration = concentration;
    }

    public double getExposure() {
        return exposure;
    }

    public void setExposure(double exposure) {
        this.exposure = exposure;
    }
}
