package com.travel.transport;

import com.travel.enums.SeatClass;
import com.travel.enums.TransportType;
import com.travel.layout.BerthBayLayout;
import com.travel.layout.CabinLayout;
import com.travel.layout.ChairLayout;

/**
 * Demonstrates: Inheritance (extends Transport) and Multimodal Class Configuration.
 * Represents railway services offering Chair Car, Non-AC Sleeper, AC 2-Tier, and AC 1-Tier coaches.
 */
public class Train extends Transport {

    private static final long serialVersionUID = 1L;

    public Train(String operatorName) {
        super(operatorName);
        initializeLayouts();
    }

    private void initializeLayouts() {
        registerLayout(SeatClass.TRAIN_CHAIR_CAR,
                new ChairLayout(SeatClass.TRAIN_CHAIR_CAR, 10, 3, 2, new char[]{'A', 'B', 'C', 'D', 'E'}));
        registerLayout(SeatClass.TRAIN_SLEEPER,
                new BerthBayLayout(SeatClass.TRAIN_SLEEPER, 8));
        registerLayout(SeatClass.TRAIN_AC_2_TIER,
                new BerthBayLayout(SeatClass.TRAIN_AC_2_TIER, 6));
        registerLayout(SeatClass.TRAIN_AC_1_TIER,
                new CabinLayout(SeatClass.TRAIN_AC_1_TIER));
    }

    @Override
    public TransportType getTransportType() {
        return TransportType.TRAIN;
    }

    @Override
    public double getRoadFactor() {
        return TransportType.TRAIN.getRoadFactor();
    }

    @Override
    public double getAverageSpeedKmh() {
        return TransportType.TRAIN.getAverageSpeedKmh();
    }
}
