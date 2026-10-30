package com.travel.layout;

import com.travel.enums.BerthType;
import com.travel.enums.SeatClass;
import com.travel.seat.BerthSeat;

/**
 * Demonstrates: Inheritance (extends SeatLayout) and Compartment Cabin Partitioning.
 * Models Train AC 1-Tier First Class private 4-berth coupes/cabins (2 Lower + 2 Upper).
 */
public class CabinLayout extends SeatLayout {

    private static final long serialVersionUID = 1L;

    public static final int DEFAULT_CABINS = 5;
    public static final int BERTHS_PER_CABIN = 4;

    public CabinLayout(SeatClass seatClass) {
        super(seatClass, 2, DEFAULT_CABINS);
        initializeLayout();
    }

    @Override
    public void initializeLayout() {
        seats.clear();
        seatMap.clear();

        double defaultPrice = (seatClass != null) ? seatClass.getMinFare() : 0.0;
        int seatCounter = 1;

        for (int cabin = 1; cabin <= totalRows; cabin++) {
            // Two lower berths in this cabin
            String cLb1 = seatCounter + "-LB";
            registerSeat(new BerthSeat(cLb1, seatClass, defaultPrice, "Lower", BerthType.LB, cabin));
            seatCounter++;

            String cLb2 = seatCounter + "-LB";
            registerSeat(new BerthSeat(cLb2, seatClass, defaultPrice, "Lower", BerthType.LB, cabin));
            seatCounter++;

            // Two upper berths in this cabin
            String cUb1 = seatCounter + "-UB";
            registerSeat(new BerthSeat(cUb1, seatClass, defaultPrice, "Upper", BerthType.UB, cabin));
            seatCounter++;

            String cUb2 = seatCounter + "-UB";
            registerSeat(new BerthSeat(cUb2, seatClass, defaultPrice, "Upper", BerthType.UB, cabin));
            seatCounter++;
        }
    }
}
