package com.travel.layout;

import com.travel.enums.BerthType;
import com.travel.enums.SeatClass;
import com.travel.seat.BerthSeat;

/**
 * Demonstrates: Inheritance (extends SeatLayout), Polymorphism, and Bay-Oriented Seating.
 * Models Indian Railways bay-partitioned sleeper coaches (Main bay + Side berths across 2 tiers).
 */
public class BerthBayLayout extends SeatLayout {

    private static final long serialVersionUID = 1L;

    private final int totalBays;

    public BerthBayLayout(SeatClass seatClass, int totalBays) {
        super(seatClass, 2, totalBays);
        this.totalBays = totalBays;
        initializeLayout();
    }

    @Override
    public void initializeLayout() {
        seats.clear();
        seatMap.clear();

        double defaultPrice = (seatClass != null) ? seatClass.getMinFare() : 0.0;
        int seatIndex = 1;

        for (int bay = 1; bay <= totalBays; bay++) {
            // Main Bay Lower Berths (Lower Deck)
            String num1 = seatIndex + "-LB";
            registerSeat(new BerthSeat(num1, seatClass, defaultPrice, "Lower", BerthType.LB, bay));
            seatIndex++;

            String num2 = seatIndex + "-LB";
            registerSeat(new BerthSeat(num2, seatClass, defaultPrice, "Lower", BerthType.LB, bay));
            seatIndex++;

            // Main Bay Upper Berths (Upper Deck)
            String num3 = seatIndex + "-UB";
            registerSeat(new BerthSeat(num3, seatClass, defaultPrice, "Upper", BerthType.UB, bay));
            seatIndex++;

            String num4 = seatIndex + "-UB";
            registerSeat(new BerthSeat(num4, seatClass, defaultPrice, "Upper", BerthType.UB, bay));
            seatIndex++;

            // Side Berths
            String num5 = seatIndex + "-SL";
            registerSeat(new BerthSeat(num5, seatClass, defaultPrice, "Lower", BerthType.SL, bay));
            seatIndex++;

            String num6 = seatIndex + "-SU";
            registerSeat(new BerthSeat(num6, seatClass, defaultPrice, "Upper", BerthType.SU, bay));
            seatIndex++;
        }
    }

    public int getTotalBays() {
        return totalBays;
    }
}
