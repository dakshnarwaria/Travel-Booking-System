package com.travel.transport;

import com.travel.enums.SeatClass;
import com.travel.enums.TransportType;
import com.travel.layout.ChairLayout;
import com.travel.layout.SleeperDeckLayout;

/**
 * Demonstrates: Inheritance (extends Transport) and Polymorphism.
 * Represents bus operators offering Seater, AC Seater, Sleeper, and AC Sleeper services.
 */
public class Bus extends Transport {

    private static final long serialVersionUID = 1L;

    public Bus(String operatorName) {
        super(operatorName);
        initializeLayouts();
    }

    private void initializeLayouts() {
        registerLayout(SeatClass.BUS_SEATER,
                new ChairLayout(SeatClass.BUS_SEATER, 10, 2, 2, new char[]{'A', 'B', 'C', 'D'}));
        registerLayout(SeatClass.BUS_AC_SEATER,
                new ChairLayout(SeatClass.BUS_AC_SEATER, 10, 2, 2, new char[]{'A', 'B', 'C', 'D'}));
        registerLayout(SeatClass.BUS_SLEEPER,
                new SleeperDeckLayout(SeatClass.BUS_SLEEPER));
        registerLayout(SeatClass.BUS_AC_SLEEPER,
                new SleeperDeckLayout(SeatClass.BUS_AC_SLEEPER));
    }

    @Override
    public TransportType getTransportType() {
        return TransportType.BUS;
    }

    @Override
    public double getRoadFactor() {
        return TransportType.BUS.getRoadFactor();
    }

    @Override
    public double getAverageSpeedKmh() {
        return TransportType.BUS.getAverageSpeedKmh();
    }
}
