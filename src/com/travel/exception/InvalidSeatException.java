package com.travel.exception;

/**
 * Demonstrates: Hierarchical Checked Exception.
 * Thrown when a user supplies a non-existent, malformed, or out-of-bounds seat identifier.
 */
public class InvalidSeatException extends TravelException {
    private final String seatNumber;

    public InvalidSeatException(String seatNumber) {
        super("Seat identifier '" + seatNumber + "' is invalid for this layout.");
        this.seatNumber = seatNumber;
    }

    public InvalidSeatException(String seatNumber, String message) {
        super(message);
        this.seatNumber = seatNumber;
    }

    public String getSeatNumber() {
        return seatNumber;
    }
}
