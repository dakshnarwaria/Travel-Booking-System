package com.travel.enums;

/**
 * Demonstrates: State Representation via Enumeration.
 * Encapsulates the availability lifecycle states of a seat.
 */
public enum SeatStatus {
    AVAILABLE("Available"),
    BOOKED("Booked"),
    SELECTED("Selected");

    private final String label;

    SeatStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
