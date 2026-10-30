package com.travel.exception;

/**
 * Demonstrates: Hierarchical Checked Exception and Exception Chaining.
 * Thrown when an operation attempts to select or reserve a seat that has already been taken.
 */
public class SeatAlreadyBookedException extends TravelException {
    private final String seatNumber;

    public SeatAlreadyBookedException(String seatNumber) {
        super("Seat '" + seatNumber + "' is already booked. Please choose another seat.");
        this.seatNumber = seatNumber;
    }

    public SeatAlreadyBookedException(String seatNumber, String message) {
        super(message);
        this.seatNumber = seatNumber;
    }

    public String getSeatNumber() {
        return seatNumber;
    }
}
