package com.travel.enums;

/**
 * Demonstrates: Enums with fields and methods, Encapsulation.
 * Defines the supported modes of transport along with mode-specific calculation factors.
 */
public enum TransportType {
    BUS("Bus", 1.25, 55.0),
    TRAIN("Train", 1.20, 65.0),
    FLIGHT("Flight", 1.00, 700.0);

    private final String displayName;
    private final double roadFactor;
    private final double averageSpeedKmh;

    TransportType(String displayName, double roadFactor, double averageSpeedKmh) {
        this.displayName = displayName;
        this.roadFactor = roadFactor;
        this.averageSpeedKmh = averageSpeedKmh;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getRoadFactor() {
        return roadFactor;
    }

    public double getAverageSpeedKmh() {
        return averageSpeedKmh;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
