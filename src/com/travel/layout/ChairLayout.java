package com.travel.layout;

import com.travel.enums.SeatClass;
import com.travel.seat.ChairSeat;

/**
 * Demonstrates: Inheritance (extends SeatLayout) and Polymorphic Initialization.
 * Models upright row-and-column seating layouts with an aisle (e.g. 2+2 for bus, 3+2 for train).
 */
public class ChairLayout extends SeatLayout {

    private static final long serialVersionUID = 1L;

    private final int leftCols;
    private final int rightCols;
    private final char[] letters;

    public ChairLayout(SeatClass seatClass, int totalRows, int leftCols, int rightCols, char[] letters) {
        super(seatClass, 1, totalRows);
        this.leftCols = leftCols;
        this.rightCols = rightCols;
        this.letters = letters;
        initializeLayout();
    }

    @Override
    public void initializeLayout() {
        seats.clear();
        seatMap.clear();

        double defaultPrice = (seatClass != null) ? seatClass.getMinFare() : 0.0;
        int totalCols = leftCols + rightCols;

        for (int row = 1; row <= totalRows; row++) {
            for (int col = 0; col < totalCols; col++) {
                char letter = (col < letters.length) ? letters[col] : (char) ('A' + col);
                String seatNum = row + String.valueOf(letter);

                boolean isWindow = (col == 0 || col == totalCols - 1);
                boolean isAisle = (col == leftCols - 1 || col == leftCols);

                ChairSeat seat = new ChairSeat(seatNum, seatClass, defaultPrice, "Lower", isWindow, isAisle, false);
                registerSeat(seat);
            }
        }
    }

    public int getLeftCols() {
        return leftCols;
    }

    public int getRightCols() {
        return rightCols;
    }
}
