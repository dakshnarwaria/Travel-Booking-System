package com.travel.model;

import com.travel.util.ConsoleColors;
import com.travel.util.Constants;
import com.travel.util.IdGenerator;
import com.travel.payment.Payment;
import com.travel.payment.PaymentStatus;
import com.travel.ui.ScreenStyle;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates: Object Creation from Aggregation, Information Presentation,
 * and Domain Object Modeling.
 * Encapsulates an issued passenger ticket generated from a confirmed booking.
 */
public class Ticket implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final String ticketId;
    private final Booking booking;
    private final LocalDateTime issueTime;

    public Ticket(Booking booking) {
        this.ticketId = IdGenerator.generateTicketId();
        this.booking = booking;
        this.issueTime = LocalDateTime.now();
    }

    public String getTicketId() {
        return ticketId;
    }

    public Booking getBooking() {
        return booking;
    }

    public LocalDateTime getIssueTime() {
        return issueTime;
    }

    /**
     * Renders a formatted printable e-ticket to the console.
     */
    public void printTicket() {
        Trip trip = booking.getTrip();
        Route route = trip.getRoute();

        List<String> lines = new ArrayList<>();
        String title = "ELECTRONIC TRAVEL TICKET - "
                + booking.getStatus().toString().toUpperCase();
        lines.add(ConsoleColors.colorize(ScreenStyle.center(title, 74),
                ConsoleColors.GREEN, ConsoleColors.BOLD));
        lines.add(ScreenStyle.ruleContent());
        lines.add("PNR: " + booking.getPnr());
        lines.add("Ticket ID: " + ticketId);
        lines.add("Booking time: " + issueTime.format(DATE_TIME_FORMATTER));
        lines.add("Status: " + booking.getStatus());
        lines.add(ScreenStyle.ruleContent());
        lines.add("Operator: " + trip.getTransport().getOperatorName());
        lines.add("Service mode: " + route.getTransportType().getDisplayName());
        lines.add("From: " + route.getSource().getName());
        lines.add("To: " + route.getDestination().getName());
        lines.add(String.format("Travel date: %s | Distance: %.1f km",
                trip.getDate().format(Trip.DATE_FORMATTER), route.getDistanceKm()));
        lines.add("Departure: " + trip.getDepartureTime().format(Trip.TIME_FORMATTER));
        lines.add("Arrival: " + trip.getFormattedArrival());
        lines.add("Travel class: " + booking.getSeatClass().getDisplayName());
        lines.add("Total seats: " + booking.getSeats().size());
        lines.add("Seat numbers: " + String.join(", ", booking.getSeatNumbers()));
        Payment payment = booking.getPayment();
        if (payment != null) {
            lines.add("Payment mode: " + payment.getPaymentMode().getDisplayName());
            lines.add("Payment status: " + payment.getStatus());
            lines.add("Transaction ID: " + payment.getTransactionId());
        }
        lines.add(ScreenStyle.ruleContent());
        lines.add(ConsoleColors.colorize("PASSENGER MANIFEST:",
                ConsoleColors.WHITE, ConsoleColors.BOLD));

        List<Passenger> passengers = booking.getPassengers();
        for (int i = 0; i < passengers.size(); i++) {
            Passenger p = passengers.get(i);
            lines.add(String.format("%d. Seat: %s | %s | Age: %d | Gender: %s",
                    i + 1, p.getSeatNumber(), p.getName(), p.getAge(), p.getGender()));
        }

        lines.add(ScreenStyle.ruleContent());
        String fareLabel = payment != null && payment.getStatus() == PaymentStatus.PAY_AT_ARRIVAL
                ? "TOTAL FARE DUE:"
                : "TOTAL FARE PAID:";
        lines.add(ConsoleColors.colorize(
                String.format("%s %s%.0f", fareLabel, Constants.CURRENCY,
                        booking.getTotalFare()),
                ConsoleColors.GREEN, ConsoleColors.BOLD));
        lines.add("Keep your PNR safe to view or cancel this ticket.");
        for (String line : ScreenStyle.frame(lines)) {
            System.out.println(line);
        }
        System.out.println();
    }

    @Override
    public String toString() {
        return String.format("Ticket [%s] PNR: %s (%s)", ticketId, booking.getPnr(), booking.getStatus());
    }
}
