package com.travel.model;

import com.travel.enums.BookingStatus;
import com.travel.enums.SeatClass;
import com.travel.seat.Seat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Demonstrates: Aggregation and Composition (has-a Trip, has-a List<Seat>, has-a List<Passenger>),
 * Encapsulation, and Booking Lifecycle State Management.
 */
public class Booking implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final String pnr;
    private final Trip trip;
    private final SeatClass seatClass;
    private final List<Seat> seats;
    private final List<Passenger> passengers;
    private double totalFare;
    private BookingStatus status;
    private final LocalDateTime bookingTime;

    public Booking(String pnr, Trip trip, SeatClass seatClass, List<Seat> seats,
                   List<Passenger> passengers, double totalFare) {
        this.pnr = pnr;
        this.trip = trip;
        this.seatClass = seatClass;
        this.seats = (seats != null) ? new ArrayList<>(seats) : new ArrayList<>();
        this.passengers = (passengers != null) ? new ArrayList<>(passengers) : new ArrayList<>();
        this.totalFare = totalFare;
        this.status = BookingStatus.PENDING;
        this.bookingTime = LocalDateTime.now();
    }

    public String getPnr() {
        return pnr;
    }

    public Trip getTrip() {
        return trip;
    }

    public SeatClass getSeatClass() {
        return seatClass;
    }

    public List<Seat> getSeats() {
        return Collections.unmodifiableList(seats);
    }

    public List<Passenger> getPassengers() {
        return Collections.unmodifiableList(passengers);
    }

    public double getTotalFare() {
        return totalFare;
    }

    public void setTotalFare(double totalFare) {
        this.totalFare = totalFare;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public List<String> getSeatNumbers() {
        List<String> list = new ArrayList<>();
        for (Seat s : seats) {
            list.add(s.getSeatNumber());
        }
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;
        Booking booking = (Booking) o;
        return Objects.equals(pnr, booking.pnr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pnr);
    }

    @Override
    public String toString() {
        return String.format("PNR: %s | %s | %s -> %s | Seats: %s | Total: ₹%.0f | Status: %s",
                pnr, trip.getTransport().getOperatorName(),
                trip.getRoute().getSource().getName(), trip.getRoute().getDestination().getName(),
                String.join(", ", getSeatNumbers()), totalFare, status);
    }
}
