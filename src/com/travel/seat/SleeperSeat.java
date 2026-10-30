package com.travel.seat;

import com.travel.enums.SeatClass;
import com.travel.enums.SeatStatus;
import com.travel.enums.SeatType;
import com.travel.util.ConsoleColors;

/**
 * Demonstrates: Inheritance (extends Seat) and Runtime Polymorphism (overrides render()).
 * Represents a horizontal sleeping berth/bunk in sleeper coaches.
 */
public class SleeperSeat extends Seat {

    private static final long serialVersionUID = 1L;

    public SleeperSeat(String seatNumber, SeatClass seatClass, double price, String deckLabel) {
        super(seatNumber, seatClass, SeatType.SLEEPER, price, deckLabel);
    }

    protected SleeperSeat(String seatNumber, SeatClass seatClass, SeatType seatType, double price, String deckLabel) {
        super(seatNumber, seatClass, seatType, price, deckLabel);
    }

    /**
     * Polymorphic rendering of an individual sleeper block.
     */
    @Override
    public void render() {
        String color = getDisplayColor();
        String seatNum = getSeatNumber();
        String numDisplay = (getStatus() == SeatStatus.BOOKED)
                ? String.format(" X%-4s", seatNum.length() > 4 ? seatNum.substring(0, 4) : seatNum)
                : String.format(" %-5s", seatNum);

        String pillowLine = (getStatus() == SeatStatus.BOOKED)
                ? " XXXXX "
                : (ConsoleColors.isAsciiMode() ? " ===== " : " ▂▂▂▂▂ ");

        if (ConsoleColors.isAsciiMode()) {
            System.out.println(ConsoleColors.colorize("+-------+", color));
            System.out.println(ConsoleColors.colorize("|" + numDisplay + "|", color));
            System.out.println(ConsoleColors.colorize("|" + pillowLine + "|", color));
            System.out.println(ConsoleColors.colorize("+-------+", color));
        } else {
            System.out.println(ConsoleColors.colorize("┌───────┐", color));
            System.out.println(ConsoleColors.colorize("│" + numDisplay + "│", color));
            System.out.println(ConsoleColors.colorize("│" + pillowLine + "│", color));
            System.out.println(ConsoleColors.colorize("└───────┘", color));
        }
        String priceDisplay = (getStatus() == SeatStatus.BOOKED)
                ? "  Sold  "
                : String.format(" ₹%.0f ", getPrice());
        System.out.println(ConsoleColors.colorize(priceDisplay, color));
    }
}
