package com.travel.enums;

/**
 * Demonstrates: State Representation via Enumeration.
 * Models the lifecycle status of a ticket reservation.
 */
public enum BookingStatus {
    PENDING("Pending Payment"),
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled");

    private final String description;

    BookingStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
