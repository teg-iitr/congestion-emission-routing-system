package com.map.app.service;

public enum ModeInhalationRate {
    car(0.66),
    motorcycle(0.66),
    foot(10),
    bike(3.06);

    private final double numVal;

    ModeInhalationRate(double numVal) {
        this.numVal = numVal;
    }

    public double getNumVal() {
        return numVal;
    }
}
