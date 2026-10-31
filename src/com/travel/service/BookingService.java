package com.travel.service;

import com.travel.enums.BookingStatus;
import com.travel.enums.SeatClass;
import com.travel.enums.SeatStatus;
import com.travel.exception.InvalidSeatException;
import com.travel.exception.SeatAlreadyBookedException;
import com.travel.layout.SeatLayout;
import com.travel.model.Booking;
import com.travel.model.Passenger;
import com.travel.model.Ticket;
import com.travel.model.Trip;
import com.travel.payment.PaymentStatus;
import com.travel.seat.Seat;
import com.travel.util.IdGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates: Compile-Time Polymorphism (Method Overloading & Varargs),
 * State Management, and Service-Level Transaction Processing.
 * Orchestrates seat reservations, passenger manifests, and ticket issuance.
 */
public class BookingService {

    private final Map<String, Booking> bookingRegistry = new HashMap<>();
    private final Map<String, List<Trip>> sessionTripCache = new HashMap<>();

    public BookingService() {}

    /**
     * Session cache ensuring modified seat states persist across searches in the same runtime session.
     */
    public List<Trip> getOrGenerateTrips(com.travel.model.Route route, java.time.LocalDate date) {
        String key = String.format("%s_%s_%s_%s",
                route.getSource().getName().toLowerCase(),
                route.getDestination().getName().toLowerCase(),
                route.getTransportType().name(),
                date.toString());

        if (!sessionTripCache.containsKey(key)) {
            List<Trip> generated = TripGenerator.generateTrips(route, date);
            sessionTripCache.put(key, generated);
        }
        return sessionTripCache.get(key);
    }

    // -------------------------------------------------------------------------
    // Compile-time Polymorphism: Overloaded booking helper methods & varargs
    // -------------------------------------------------------------------------

    /**
     * Overloaded method 1: Validate a single seat for reservation.
     */
    public Seat bookSeat(SeatLayout layout, String seatNumber) throws InvalidSeatException, SeatAlreadyBookedException {
        if (layout == null || seatNumber == null) {
            throw new InvalidSeatException("null", "Layout or seat identifier cannot be null.");
        }
        Seat seat = layout.getSeat(seatNumber);
        if (!seat.isAvailable()) {
            throw new SeatAlreadyBookedException(seat.getSeatNumber());
        }
        return seat;
    }

    /**
     * Overloaded method 2: Validate a single seat and bind passenger data.
     */
    public Seat bookSeat(SeatLayout layout, String seatNumber, Passenger passenger)
            throws InvalidSeatException, SeatAlreadyBookedException {
        Seat seat = bookSeat(layout, seatNumber);
        if (passenger != null) {
            passenger.setSeatNumber(seat.getSeatNumber());
        }
        return seat;
    }

    /**
     * Overloaded method 3: Demonstrates Java Varargs. Validates multiple seats at once.
     */
    public List<Seat> bookSeats(SeatLayout layout, String... seatNumbers)
            throws InvalidSeatException, SeatAlreadyBookedException {
        if (layout == null || seatNumbers == null) {
            return Collections.emptyList();
        }
        List<Seat> validated = new ArrayList<>();
        for (String num : seatNumbers) {
            if (num != null && !num.trim().isEmpty()) {
                validated.add(bookSeat(layout, num.trim()));
            }
        }
        return validated;
    }

    // -------------------------------------------------------------------------
    // Booking Lifecycle
    // -------------------------------------------------------------------------

    /**
     * Creates a new pending booking with computed total fare.
     */
    public Booking createBooking(Trip trip, SeatClass seatClass, List<Seat> seats, List<Passenger> passengers) {
        String pnr = IdGenerator.generatePnr();
        double distanceKm = trip.getRoute().getDistanceKm();
        double totalFare = 0.0;

        for (Seat seat : seats) {
            PricingService.FareBreakdown fb = PricingService.calculateFare(seatClass, distanceKm, seat);
            totalFare += fb.getTotalFare();
        }

        Booking booking = new Booking(pnr, trip, seatClass, seats, passengers, totalFare);
        bookingRegistry.put(pnr, booking);
        return booking;
    }

    /**
     * Confirms the booking, marks seats as BOOKED, and issues an Electronic Ticket.
     */
    public Ticket confirmBooking(Booking booking) {
        if (booking == null) return null;
        if (booking.getPayment() == null
                || (booking.getPayment().getStatus() != PaymentStatus.PAID
                && booking.getPayment().getStatus() != PaymentStatus.PAY_AT_ARRIVAL)) {
            throw new IllegalStateException("A booking can only be confirmed after successful payment.");
        }
        for (Seat seat : booking.getSeats()) {
            if (seat.getStatus() == SeatStatus.BOOKED) {
                throw new IllegalStateException("Seat " + seat.getSeatNumber() + " was booked before confirmation.");
            }
        }

        // Transition status and commit seat reservations
        booking.setStatus(BookingStatus.CONFIRMED);
        for (Seat seat : booking.getSeats()) {
            seat.setStatus(SeatStatus.BOOKED);
        }

        return new Ticket(booking);
    }

    public Booking getBookingByPnr(String pnr) {
        if (pnr == null) return null;
        return bookingRegistry.get(pnr.trim().toUpperCase());
    }

    public Map<String, Booking> getAllBookings() {
        return Collections.unmodifiableMap(bookingRegistry);
    }
}
