package com.travel.layout;

import com.travel.enums.SeatClass;
import com.travel.enums.SeatStatus;
import com.travel.exception.InvalidSeatException;
import com.travel.seat.Seat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates: Abstraction (Abstract Class), Composition (has-a List<Seat>),
 * Encapsulation, and Generics.
 * Serves as the base specification for vehicle coach seating architectures.
 */
public abstract class SeatLayout implements Serializable {

    private static final long serialVersionUID = 1L;

    protected SeatClass seatClass;
    protected int totalDecks;
    protected int totalRows;
    protected final List<Seat> seats = new ArrayList<>();
    protected final Map<String, Seat> seatMap = new LinkedHashMap<>();

    public SeatLayout(SeatClass seatClass, int totalDecks, int totalRows) {
        this.seatClass = seatClass;
        this.totalDecks = totalDecks;
        this.totalRows = totalRows;
    }

    /**
     * Initializes the geometric arrangement of seats.
     */
    public abstract void initializeLayout();

    protected void registerSeat(Seat seat) {
        if (seat != null) {
            seats.add(seat);
            seatMap.put(seat.getSeatNumber().toUpperCase(), seat);
        }
    }

    public List<Seat> getAllSeats() {
        return Collections.unmodifiableList(seats);
    }

    public List<Seat> getSeatsByDeck(String deckLabel) {
        List<Seat> deckSeats = new ArrayList<>();
        for (Seat seat : seats) {
            if (seat.getDeckLabel().equalsIgnoreCase(deckLabel)) {
                deckSeats.add(seat);
            }
        }
        return deckSeats;
    }

    public Seat getSeat(String seatNumber) throws InvalidSeatException {
        if (seatNumber == null) {
            throw new InvalidSeatException("null", "Seat number cannot be null.");
        }
        Seat seat = seatMap.get(seatNumber.trim().toUpperCase());
        if (seat == null) {
            throw new InvalidSeatException(seatNumber.trim().toUpperCase());
        }
        return seat;
    }

    public boolean hasSeat(String seatNumber) {
        if (seatNumber == null) return false;
        return seatMap.containsKey(seatNumber.trim().toUpperCase());
    }

    public int getTotalSeats() {
        return seats.size();
    }

    public int getAvailableSeatsCount() {
        int count = 0;
        for (Seat seat : seats) {
            if (seat.isAvailable()) {
                count++;
            }
        }
        return count;
    }

    public int getBookedSeatsCount() {
        int count = 0;
        for (Seat seat : seats) {
            if (seat.getStatus() == SeatStatus.BOOKED) {
                count++;
            }
        }
        return count;
    }

    public SeatClass getSeatClass() {
        return seatClass;
    }

    public int getTotalDecks() {
        return totalDecks;
    }

    public int getTotalRows() {
        return totalRows;
    }

    /**
     * Updates the base fare across all seats in this layout.
     */
    public void updateAllSeatPrices(double price) {
        for (Seat seat : seats) {
            seat.setPrice(price);
        }
    }
}
