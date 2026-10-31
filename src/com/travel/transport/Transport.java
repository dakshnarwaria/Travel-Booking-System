package com.travel.transport;

import com.travel.enums.SeatClass;
import com.travel.enums.TransportType;
import com.travel.interfaces.Priceable;
import com.travel.layout.SeatLayout;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Demonstrates: Abstraction (Abstract Class), Interface Realization (Priceable),
 * Composition (Map of SeatClass to SeatLayout), and Polymorphism.
 * Base class for all physical vehicular transport carriers.
 */
public abstract class Transport implements Priceable, Serializable {

    private static final long serialVersionUID = 1L;

    protected String operatorName;
    protected final LinkedHashMap<SeatClass, SeatLayout> seatLayouts = new LinkedHashMap<>();

    public Transport(String operatorName) {
        this.operatorName = (operatorName != null) ? operatorName.trim() : "Standard Carrier";
    }

    public String getOperatorName() {
        return operatorName;
    }

    public abstract TransportType getTransportType();

    public abstract double getRoadFactor();

    public abstract double getAverageSpeedKmh();

    public Map<SeatClass, SeatLayout> getSeatLayouts() {
        return Collections.unmodifiableMap(seatLayouts);
    }

    public SeatLayout getSeatLayout(SeatClass seatClass) {
        return seatLayouts.get(seatClass);
    }

    protected final void registerLayout(SeatClass seatClass, SeatLayout layout) {
        if (seatClass != null && layout != null) {
            seatLayouts.put(seatClass, layout);
        }
    }

    /**
     * Calculates the baseline starting fare across available seat classes.
     */
    @Override
    public double calculateFare() {
        if (seatLayouts.isEmpty()) return 0.0;
        double min = Double.MAX_VALUE;
        for (SeatClass sc : seatLayouts.keySet()) {
            if (sc.getMinFare() < min) {
                min = sc.getMinFare();
            }
        }
        return min;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", operatorName, getTransportType().getDisplayName());
    }
}
