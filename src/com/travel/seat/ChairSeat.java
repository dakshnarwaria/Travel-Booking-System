package com.travel.seat;

import com.travel.enums.SeatClass;
import com.travel.enums.SeatStatus;
import com.travel.enums.SeatType;
import com.travel.util.ConsoleColors;

/**
 * Demonstrates: Single Inheritance and Runtime Polymorphism (Method Overriding).
 * Represents a standard or premium upright passenger chair.
 */
public class ChairSeat extends Seat {

    private static final long serialVersionUID = 1L;

    private boolean windowSeat;
    private boolean aisleSeat;
    private boolean exitRow;

    public ChairSeat(String seatNumber, SeatClass seatClass, double price, String deckLabel) {
        super(seatNumber, seatClass, SeatType.CHAIR, price, deckLabel);
    }

    public ChairSeat(String seatNumber, SeatClass seatClass, double price, String deckLabel,
                     boolean windowSeat, boolean aisleSeat, boolean exitRow) {
        super(seatNumber, seatClass, SeatType.CHAIR, price, deckLabel);
        this.windowSeat = windowSeat;
        this.aisleSeat = aisleSeat;
        this.exitRow = exitRow;
    }

    public boolean isWindowSeat() {
        return windowSeat;
    }

    public void setWindowSeat(boolean windowSeat) {
        this.windowSeat = windowSeat;
    }

    public boolean isAisleSeat() {
        return aisleSeat;
    }

    public void setAisleSeat(boolean aisleSeat) {
        this.aisleSeat = aisleSeat;
    }

    public boolean isExitRow() {
        return exitRow;
    }

    public void setExitRow(boolean exitRow) {
        this.exitRow = exitRow;
    }

    /**
     * Polymorphic rendering of an individual chair seat box.
     */
    @Override
    public void render() {
        String color = getDisplayColor();
        String seatNum = getSeatNumber();
        String numDisplay = (getStatus() == SeatStatus.BOOKED)
                ? String.format("X%-2s", seatNum.length() > 2 ? seatNum.substring(0, 2) : seatNum)
                : String.format("%-3s", seatNum);

        if (ConsoleColors.isAsciiMode()) {
            System.out.println(ConsoleColors.colorize("+---+", color));
            System.out.println(ConsoleColors.colorize("|" + numDisplay + "|", color));
            System.out.println(ConsoleColors.colorize("+---+", color));
        } else {
            System.out.println(ConsoleColors.colorize("┌───┐", color));
            System.out.println(ConsoleColors.colorize("│" + numDisplay + "│", color));
            System.out.println(ConsoleColors.colorize("└───┘", color));
        }
        String priceDisplay = (getStatus() == SeatStatus.BOOKED)
                ? "Sold"
                : String.format("₹%.0f", getPrice());
        System.out.println(ConsoleColors.colorize(priceDisplay, color));
    }
}
