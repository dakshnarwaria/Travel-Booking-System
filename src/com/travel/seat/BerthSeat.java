package com.travel.seat;

import com.travel.enums.BerthType;
import com.travel.enums.SeatClass;
import com.travel.enums.SeatStatus;
import com.travel.enums.SeatType;
import com.travel.exception.InvalidInputException;
import com.travel.util.ConsoleColors;

/**
 * Demonstrates: Multilevel Inheritance (Seat -> SleeperSeat -> BerthSeat),
 * Encapsulation, and Runtime Polymorphism.
 * Represents dedicated train coach berths with specific spatial orientations (LB, UB, SL, SU).
 */
public class BerthSeat extends SleeperSeat {

    private static final long serialVersionUID = 1L;

    private BerthType berthType;
    private int bayNumber;

    public BerthSeat(String seatNumber, SeatClass seatClass, double price, String deckLabel,
                     BerthType berthType, int bayNumber) {
        super(seatNumber, seatClass, SeatType.BERTH, price, deckLabel);
        if (berthType == null) {
            throw new InvalidInputException("Berth type cannot be null.");
        }
        if (bayNumber < 1) {
            throw new InvalidInputException("Bay number must be positive: " + bayNumber);
        }
        this.berthType = berthType;
        this.bayNumber = bayNumber;
    }

    public BerthType getBerthType() {
        return berthType;
    }

    public void setBerthType(BerthType berthType) {
        if (berthType == null) {
            throw new InvalidInputException("Berth type cannot be null.");
        }
        this.berthType = berthType;
    }

    public int getBayNumber() {
        return bayNumber;
    }

    public void setBayNumber(int bayNumber) {
        if (bayNumber < 1) {
            throw new InvalidInputException("Bay number must be positive: " + bayNumber);
        }
        this.bayNumber = bayNumber;
    }

    /**
     * Polymorphic rendering of an individual train berth block.
     * Displays seat number and berth type code (e.g., LB, UB).
     */
    @Override
    public void render() {
        String color = getDisplayColor();
        String seatNum = getSeatNumber();
        String numDisplay = (getStatus() == SeatStatus.BOOKED)
                ? String.format(" X%-4s", seatNum.length() > 4 ? seatNum.substring(0, 4) : seatNum)
                : String.format(" %-5s", seatNum);

        String tagLine = (getStatus() == SeatStatus.BOOKED)
                ? " XXXXX "
                : String.format(" [%s]  ", berthType.getCode());

        if (ConsoleColors.isAsciiMode()) {
            System.out.println(ConsoleColors.colorize("+-------+", color));
            System.out.println(ConsoleColors.colorize("|" + numDisplay + "|", color));
            System.out.println(ConsoleColors.colorize("|" + tagLine + "|", color));
            System.out.println(ConsoleColors.colorize("+-------+", color));
        } else {
            System.out.println(ConsoleColors.colorize("┌───────┐", color));
            System.out.println(ConsoleColors.colorize("│" + numDisplay + "│", color));
            System.out.println(ConsoleColors.colorize("│" + tagLine + "│", color));
            System.out.println(ConsoleColors.colorize("└───────┘", color));
        }
        String priceDisplay = (getStatus() == SeatStatus.BOOKED)
                ? "  Sold  "
                : String.format(" ₹%.0f ", getPrice());
        System.out.println(ConsoleColors.colorize(priceDisplay, color));
    }

    @Override
    public String toString() {
        return String.format("%s (%s Bay-%d) [%s, ₹%.0f, %s]",
                getSeatNumber(), berthType.getDescription(), bayNumber,
                getDeckLabel(), getPrice(), getStatus());
    }
}
