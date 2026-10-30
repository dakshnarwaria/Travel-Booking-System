package com.travel.seat;

import com.travel.enums.SeatClass;
import com.travel.enums.SeatStatus;
import com.travel.enums.SeatType;
import com.travel.exception.InvalidInputException;
import com.travel.exception.SeatAlreadyBookedException;
import com.travel.interfaces.Bookable;
import com.travel.interfaces.Displayable;
import com.travel.util.ConsoleColors;

import java.io.Serializable;
import java.util.Objects;

/**
 * Demonstrates: Abstraction, Encapsulation, Multiple Interface Implementation,
 * and Polymorphism (Abstract Method).
 * Abstract root of all physical seat representations in the system.
 */
public abstract class Seat implements Displayable, Bookable, Serializable {

    private static final long serialVersionUID = 1L;

    private String seatNumber;
    private SeatClass seatClass;
    private SeatType seatType;
    private double price;
    private SeatStatus status;
    private String deckLabel;

    public Seat(String seatNumber, SeatClass seatClass, SeatType seatType, double price, String deckLabel) {
        setSeatNumber(seatNumber);
        setSeatClass(seatClass);
        setSeatType(seatType);
        setPrice(price);
        setDeckLabel(deckLabel);
        this.status = SeatStatus.AVAILABLE;
    }

    // Getters and Setters with Validation (Encapsulation)

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        if (seatNumber == null || seatNumber.trim().isEmpty()) {
            throw new InvalidInputException("Seat number cannot be null or empty.");
        }
        this.seatNumber = seatNumber.trim().toUpperCase();
    }

    public SeatClass getSeatClass() {
        return seatClass;
    }

    public void setSeatClass(SeatClass seatClass) {
        if (seatClass == null) {
            throw new InvalidInputException("Seat class cannot be null.");
        }
        this.seatClass = seatClass;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    public void setSeatType(SeatType seatType) {
        if (seatType == null) {
            throw new InvalidInputException("Seat type cannot be null.");
        }
        this.seatType = seatType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price < 0) {
            throw new InvalidInputException("Seat price cannot be negative: " + price);
        }
        this.price = price;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        if (status == null) {
            throw new InvalidInputException("Seat status cannot be null.");
        }
        this.status = status;
    }

    public String getDeckLabel() {
        return deckLabel;
    }

    public void setDeckLabel(String deckLabel) {
        this.deckLabel = (deckLabel == null || deckLabel.trim().isEmpty()) ? "Main" : deckLabel.trim();
    }

    // Bookable Interface Implementation

    @Override
    public boolean book() throws SeatAlreadyBookedException {
        if (this.status == SeatStatus.BOOKED) {
            throw new SeatAlreadyBookedException(this.seatNumber);
        }
        this.status = SeatStatus.BOOKED;
        return true;
    }

    @Override
    public boolean cancel() {
        if (this.status == SeatStatus.BOOKED || this.status == SeatStatus.SELECTED) {
            this.status = SeatStatus.AVAILABLE;
            return true;
        }
        return false;
    }

    @Override
    public boolean isAvailable() {
        return this.status == SeatStatus.AVAILABLE;
    }

    /**
     * Determines the appropriate ANSI display color based on availability and premium status.
     */
    public String getDisplayColor() {
        if (status == SeatStatus.BOOKED) {
            return ConsoleColors.COLOR_BOOKED;
        } else if (status == SeatStatus.SELECTED) {
            return ConsoleColors.COLOR_SELECTED;
        } else if (seatClass != null && seatClass.isPremium()) {
            return ConsoleColors.COLOR_PREMIUM;
        } else {
            return ConsoleColors.COLOR_STANDARD;
        }
    }

    // Displayable Interface Implementation (Overridden Polymorphically by Subclasses)

    @Override
    public abstract void render();

    // Object Identity and Representation

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Seat)) return false;
        Seat seat = (Seat) o;
        return Objects.equals(seatNumber, seat.seatNumber) &&
               Objects.equals(deckLabel, seat.deckLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seatNumber, deckLabel);
    }

    @Override
    public String toString() {
        return String.format("%s [%s, %s, ₹%.0f, %s]",
                seatNumber, seatClass.getDisplayName(), deckLabel, price, status);
    }
}
