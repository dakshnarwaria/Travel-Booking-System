package com.travel.model;

import com.travel.enums.BookingStatus;
import com.travel.util.ConsoleColors;
import com.travel.util.Constants;
import com.travel.util.IdGenerator;
import com.travel.payment.Payment;
import com.travel.payment.PaymentStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

        System.out.println("================================================================================");
        System.out.println(ConsoleColors.colorize(
                "                     ELECTRONIC TRAVEL TICKET - " + booking.getStatus().toString().toUpperCase(),
                ConsoleColors.GREEN,
                ConsoleColors.BOLD
        ));
        System.out.println("================================================================================");
        System.out.printf("  PNR: %-20s  TICKET ID    : %s\n", booking.getPnr(), ticketId);
        System.out.printf("  BOOKING TIME   : %-20s  STATUS       : %s\n",
                issueTime.format(DATE_TIME_FORMATTER), booking.getStatus());
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("  OPERATOR       : %-20s  SERVICE MODE : %s\n",
                trip.getTransport().getOperatorName(), route.getTransportType().getDisplayName());
        System.out.printf("  FROM           : %-20s  TO           : %s\n",
                route.getSource().getName(), route.getDestination().getName());
        System.out.printf("  TRAVEL DATE    : %-20s  DISTANCE     : %.1f km\n",
                trip.getDate().format(Trip.DATE_FORMATTER), route.getDistanceKm());
        System.out.printf("  DEPARTURE      : %-20s  ARRIVAL      : %s\n",
                trip.getDepartureTime().format(Trip.TIME_FORMATTER), trip.getFormattedArrival());
        System.out.printf("  TRAVEL CLASS   : %-20s  TOTAL SEATS  : %d\n",
                booking.getSeatClass().getDisplayName(), booking.getSeats().size());
        System.out.printf("  SEAT NUMBERS   : %s\n", String.join(", ", booking.getSeatNumbers()));
        Payment payment = booking.getPayment();
        if (payment != null) {
            System.out.printf("  PAYMENT MODE   : %-20s  PAYMENT STATUS: %s\n",
                    payment.getPaymentMode().getDisplayName(), payment.getStatus());
            System.out.printf("  TRANSACTION ID : %s\n", payment.getTransactionId());
        }
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println(ConsoleColors.colorize("  PASSENGER MANIFEST:", ConsoleColors.WHITE, ConsoleColors.BOLD));

        List<Passenger> passengers = booking.getPassengers();
        for (int i = 0; i < passengers.size(); i++) {
            Passenger p = passengers.get(i);
            System.out.printf("   %d. Seat: %-6s | %-24s | Age: %-3d | Gender: %s\n",
                    (i + 1), p.getSeatNumber(), p.getName(), p.getAge(), p.getGender());
        }

        System.out.println("--------------------------------------------------------------------------------");
        String fareLabel = payment != null && payment.getStatus() == PaymentStatus.PAY_AT_ARRIVAL
                ? "TOTAL FARE DUE:"
                : "TOTAL FARE PAID:";
        System.out.printf("  %-16s " + ConsoleColors.colorize(Constants.CURRENCY + "%.0f", ConsoleColors.GREEN, ConsoleColors.BOLD) + " (Inclusive of GST, charges & payment fee)\n",
                fareLabel, booking.getTotalFare());
        System.out.println("  Keep your PNR safe. You will need it to view or cancel this ticket.");
        System.out.println("================================================================================\n");
    }

    @Override
    public String toString() {
        return String.format("Ticket [%s] PNR: %s (%s)", ticketId, booking.getPnr(), booking.getStatus());
    }
}
