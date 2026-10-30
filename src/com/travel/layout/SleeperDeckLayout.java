package com.travel.layout;

import com.travel.enums.SeatClass;
import com.travel.seat.SleeperSeat;

/**
 * Demonstrates: Inheritance (extends SeatLayout) and Multi-Deck Layout Architecture.
 * Models bi-level sleeper bus arrangements (Lower and Upper decks with 1+2 berths per row).
 */
public class SleeperDeckLayout extends SeatLayout {

    private static final long serialVersionUID = 1L;

    public static final int DEFAULT_ROWS = 6;
    public static final int SEATS_PER_ROW = 3; // 1 left + 2 right

    public SleeperDeckLayout(SeatClass seatClass) {
        super(seatClass, 2, DEFAULT_ROWS);
        initializeLayout();
    }

    @Override
    public void initializeLayout() {
        seats.clear();
        seatMap.clear();

        double defaultPrice = (seatClass != null) ? seatClass.getMinFare() : 0.0;

        // Lower deck: L1 .. L18
        int lowerCounter = 1;
        for (int row = 1; row <= totalRows; row++) {
            for (int col = 0; col < SEATS_PER_ROW; col++) {
                String seatNum = "L" + lowerCounter++;
                SleeperSeat seat = new SleeperSeat(seatNum, seatClass, defaultPrice, "Lower");
                registerSeat(seat);
            }
        }

        // Upper deck: U1 .. U18
        int upperCounter = 1;
        for (int row = 1; row <= totalRows; row++) {
            for (int col = 0; col < SEATS_PER_ROW; col++) {
                String seatNum = "U" + upperCounter++;
                SleeperSeat seat = new SleeperSeat(seatNum, seatClass, defaultPrice, "Upper");
                registerSeat(seat);
            }
        }
    }
}
