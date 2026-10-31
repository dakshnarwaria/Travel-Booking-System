package com.travel.transport;

import com.travel.enums.SeatClass;
import com.travel.enums.TransportType;
import com.travel.layout.FlightCabinLayout;

/**
 * Demonstrates: Inheritance (extends Transport) and Aviation Multi-Class Modeling.
 * Represents airlines offering Economy and Business class cabins.
 */
public final class Flight extends Transport {

    private static final long serialVersionUID = 1L;

    public Flight(String operatorName) {
        super(operatorName);
        initializeLayouts();
    }

    private void initializeLayouts() {
        registerLayout(SeatClass.FLIGHT_ECONOMY,
                new FlightCabinLayout(SeatClass.FLIGHT_ECONOMY));
        registerLayout(SeatClass.FLIGHT_BUSINESS,
                new FlightCabinLayout(SeatClass.FLIGHT_BUSINESS));
    }

    @Override
    public TransportType getTransportType() {
        return TransportType.FLIGHT;
    }

    @Override
    public double getRoadFactor() {
        return TransportType.FLIGHT.getRoadFactor();
    }

    @Override
    public double getAverageSpeedKmh() {
        return TransportType.FLIGHT.getAverageSpeedKmh();
    }
}
