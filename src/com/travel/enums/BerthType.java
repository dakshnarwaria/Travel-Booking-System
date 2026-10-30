package com.travel.enums;

/**
 * Demonstrates: Type-safety via Enumeration.
 * Categorizes train berth positions in sleeper, 2-tier, and 1-tier coaches.
 */
public enum BerthType {
    LB("Lower Berth", "LB"),
    UB("Upper Berth", "UB"),
    SL("Side Lower", "SL"),
    SU("Side Upper", "SU");

    private final String description;
    private final String code;

    BerthType(String description, String code) {
        this.description = description;
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code;
    }
}
