package com.travel.enums;

/**
 * Demonstrates: Type-safety via Enumeration.
 * Categorizes the physical design form factor of individual seats.
 */
public enum SeatType {
    CHAIR("Chair"),
    SLEEPER("Sleeper"),
    BERTH("Berth");

    private final String displayName;

    SeatType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
