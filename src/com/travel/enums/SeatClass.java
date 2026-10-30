package com.travel.enums;

/**
 * Demonstrates: Enums with custom fields, constructors, getters, and Encapsulation.
 * Represents travel seating classes with exact rates and pricing rules.
 */
public enum SeatClass {
    // Bus classes
    BUS_SEATER(TransportType.BUS, "Seater", 1.8, 250.0, false),
    BUS_AC_SEATER(TransportType.BUS, "AC Seater", 2.4, 350.0, true),
    BUS_SLEEPER(TransportType.BUS, "Sleeper", 2.8, 400.0, false),
    BUS_AC_SLEEPER(TransportType.BUS, "AC Sleeper", 3.4, 500.0, true),

    // Train classes
    TRAIN_CHAIR_CAR(TransportType.TRAIN, "Chair Car", 1.3, 200.0, false),
    TRAIN_SLEEPER(TransportType.TRAIN, "Non-AC Sleeper", 0.8, 150.0, false),
    TRAIN_AC_2_TIER(TransportType.TRAIN, "AC 2-Tier", 2.4, 500.0, true),
    TRAIN_AC_1_TIER(TransportType.TRAIN, "AC 1-Tier", 4.0, 900.0, true),

    // Flight classes
    FLIGHT_ECONOMY(TransportType.FLIGHT, "Economy", 5.0, 2500.0, false),
    FLIGHT_BUSINESS(TransportType.FLIGHT, "Business", 14.0, 8000.0, true);

    private final TransportType transportType;
    private final String displayName;
    private final double ratePerKm;
    private final double minFare;
    private final boolean premium;

    SeatClass(TransportType transportType, String displayName, double ratePerKm, double minFare, boolean premium) {
        this.transportType = transportType;
        this.displayName = displayName;
        this.ratePerKm = ratePerKm;
        this.minFare = minFare;
        this.premium = premium;
    }

    public TransportType getTransportType() {
        return transportType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getRatePerKm() {
        return ratePerKm;
    }

    public double getMinFare() {
        return minFare;
    }

    public boolean isPremium() {
        return premium;
    }

    @Override
    public String toString() {
        return transportType.getDisplayName() + " " + displayName;
    }
}
