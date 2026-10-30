package com.travel.layout;
import com.travel.enums.SeatClass;
import com.travel.seat.ChairSeat;
import com.travel.seat.Seat;

/**
 * Demonstrates: Inheritance (extends SeatLayout) and Commercial Aviation Cabin Mapping.
 * Models single-cabin commercial aircraft seating:
 * - Economy: 12 rows of 3+3 (A, B, C | D, E, F) = 72 seats.
 * - Business: 4 rows of 2+2 (A, C | D, F) = 16 seats.
 */
public class FlightCabinLayout extends SeatLayout {

    private static final long serialVersionUID = 1L;

    public FlightCabinLayout(SeatClass seatClass) {
        super(seatClass, 1, (seatClass == SeatClass.FLIGHT_BUSINESS ? 4 : 12));
        initializeLayout();
    }

    @Override
    public void initializeLayout() {
        seats.clear();
        seatMap.clear();

        double defaultPrice = (seatClass != null) ? seatClass.getMinFare() : 0.0;

        if (seatClass == SeatClass.FLIGHT_BUSINESS) {
            // 4 rows of 2+2: A, C (left) and D, F (right)
            char[] busLetters = {'A', 'C', 'D', 'F'};
            for (int row = 1; row <= totalRows; row++) {
                for (int col = 0; col < busLetters.length; col++) {
                    char letter = busLetters[col];
                    String seatNum = row + String.valueOf(letter);
                    boolean isWindow = (col == 0 || col == 3);
                    boolean isAisle = (col == 1 || col == 2);
                    registerSeat(new ChairSeat(seatNum, seatClass, defaultPrice, "Main", isWindow, isAisle, false));
                }
            }
        } else {
            // Economy: 12 rows of 3+3: A, B, C | D, E, F
            char[] econLetters = {'A', 'B', 'C', 'D', 'E', 'F'};
            for (int row = 1; row <= totalRows; row++) {
                boolean isExit = (row == 6 || row == 7);
                double seatPrice = defaultPrice + (isExit ? 400.0 : 0.0);
                for (int col = 0; col < econLetters.length; col++) {
                    char letter = econLetters[col];
                    String seatNum = row + String.valueOf(letter);
                    boolean isWindow = (col == 0 || col == 5);
                    boolean isAisle = (col == 2 || col == 3);
                    registerSeat(new ChairSeat(seatNum, seatClass, seatPrice, "Main", isWindow, isAisle, isExit));
                }
            }
        }
    }

    @Override
    public void updateAllSeatPrices(double basePrice) {
        for (Seat seat : seats) {
            if (seat instanceof ChairSeat && ((ChairSeat) seat).isExitRow()) {
                seat.setPrice(basePrice + 400.0);
            } else {
                seat.setPrice(basePrice);
            }
        }
    }
}
