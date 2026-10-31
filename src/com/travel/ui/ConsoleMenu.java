package com.travel.ui;

import com.travel.enums.BookingStatus;
import com.travel.enums.PaymentMode;
import com.travel.enums.SeatClass;
import com.travel.enums.SeatStatus;
import com.travel.enums.TransportType;
import com.travel.exception.InvalidSeatException;
import com.travel.exception.InputEndedException;
import com.travel.exception.PaymentFailedException;
import com.travel.exception.SeatAlreadyBookedException;
import com.travel.exception.TooManyInvalidAttemptsException;
import com.travel.layout.SeatLayout;
import com.travel.model.Booking;
import com.travel.model.City;
import com.travel.model.Passenger;
import com.travel.model.Route;
import com.travel.model.Ticket;
import com.travel.model.Trip;
import com.travel.payment.Payment;
import com.travel.payment.PaymentFactory;
import com.travel.payment.PaymentStatus;
import com.travel.service.CashRefundPolicy;
import com.travel.service.PaymentService;
import com.travel.repository.CityRepository;
import com.travel.service.RefundPolicy;
import com.travel.service.TimeBasedRefundPolicy;
import com.travel.seat.Seat;
import com.travel.service.BookingService;
import com.travel.service.PricingService;
import com.travel.service.SearchService;
import com.travel.util.ConsoleColors;
import com.travel.util.Constants;
import com.travel.util.InputHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Demonstrates: UI Presentation Layer, State-Machine Menu Workflow,
 * Exception Handling & Recovery, and User Input Sanitization.
 * Implements the interactive booking flow outlined in PLAN.md Section 7.
 */
public class ConsoleMenu {

    private final CityRepository cityRepository;
    private final SearchService searchService;
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final Scanner scanner;
    private List<Seat> temporarilySelectedSeats = Collections.emptyList();

    private static final int MAX_INVALID_ATTEMPTS = 5;

    public ConsoleMenu(CityRepository cityRepository, SearchService searchService, BookingService bookingService) {
        this.cityRepository = cityRepository;
        this.searchService = searchService;
        this.bookingService = bookingService;
        this.paymentService = new PaymentService();
        this.scanner = new Scanner(System.in);
    }

    /**
     * Entry point for the main application loop.
     */
    public void start() {
        boolean running = true;
        while (running) {
            try {
                printMainMenu();
                int choice = readIntSafe("Select option", 1, 5);

                switch (choice) {
                    case 1:
                        executeBookingFlow();
                        break;
                    case 2:
                        executeViewTicket();
                        break;
                    case 3:
                        executeMyBookings();
                        break;
                    case 4:
                        executeCancelTicket();
                        break;
                    case 5:
                        System.out.println(ConsoleColors.colorize("Thank you for using the Travel Ticket Booking System. Goodbye!", ConsoleColors.CYAN));
                        running = false;
                        break;
                }
            } catch (TooManyInvalidAttemptsException e) {
                releaseSelectedSeats(temporarilySelectedSeats);
                temporarilySelectedSeats = Collections.emptyList();
                System.out.println(ConsoleColors.colorize(
                        "Too many invalid attempts. Returning to the main menu.",
                        ConsoleColors.YELLOW
                ));
            }
        }
    }

    private void printMainMenu() {
        System.out.println("\n================================================================================");
        System.out.println(ConsoleColors.colorize("             TRAVEL TICKET BOOKING SYSTEM - MAIN MENU", ConsoleColors.CYAN, ConsoleColors.BOLD));
        System.out.println("================================================================================");
        System.out.println("  1. Book Travel Ticket");
        System.out.println("  2. View Ticket by PNR");
        System.out.println("  3. My Bookings");
        System.out.println("  4. Cancel Ticket");
        System.out.println("  5. Exit System");
        System.out.println("--------------------------------------------------------------------------------");
    }

    // -------------------------------------------------------------------------
    // Steps 1 to 11 of User Journey (PLAN.md Section 7)
    // -------------------------------------------------------------------------

    private void executeBookingFlow() {
        System.out.println("\n" + ConsoleColors.colorize("--- STEP 1: ROUTE & TRAVEL DATE ---", ConsoleColors.YELLOW, ConsoleColors.BOLD));

        // 1. Source City
        City source = promptCitySelection("source");
        if (source == null) return;

        // 2. Destination City
        City destination = promptCitySelection("destination");
        if (destination == null) return;

        if (source.equals(destination)) {
            System.out.println(ConsoleColors.colorize("Source and destination cannot be the same city. Please restart.", ConsoleColors.RED));
            return;
        }

        // 3. Travel Date
        LocalDate date = promptTravelDate();
        if (date == null) return;

        // 4. Choose Mode
        List<TransportType> availableModes = searchService.getAvailableModes(source, destination);
        if (availableModes.isEmpty()) {
            System.out.println(ConsoleColors.colorize("No operational transport modes connect these cities.", ConsoleColors.RED));
            return;
        }

        System.out.println("\n" + ConsoleColors.colorize("--- STEP 2: SELECT TRANSPORT MODE ---", ConsoleColors.YELLOW, ConsoleColors.BOLD));
        for (int i = 0; i < availableModes.size(); i++) {
            System.out.printf("  %d. %s\n", (i + 1), availableModes.get(i).getDisplayName());
        }
        int modeIdx = readIntSafe("Choose transport mode", 1, availableModes.size()) - 1;
        TransportType selectedMode = availableModes.get(modeIdx);

        Route route = new Route(source, destination, selectedMode);

        // 5. Query and Choose Trip
        List<Trip> trips = bookingService.getOrGenerateTrips(route, date);
        if (trips.isEmpty()) {
            System.out.println(ConsoleColors.colorize("No trips scheduled for this date.", ConsoleColors.RED));
            return;
        }

        System.out.println("\n" + ConsoleColors.colorize("--- STEP 3: SELECT SCHEDULED TRIP ---", ConsoleColors.YELLOW, ConsoleColors.BOLD));
        System.out.printf("Trips found between %s and %s on %s:\n", source.getName(), destination.getName(), date.format(Trip.DATE_FORMATTER));
        for (int i = 0; i < trips.size(); i++) {
            Trip t = trips.get(i);
            System.out.printf("  %d. %-28s | Dep: %s | Arr: %-16s | Dur: %s\n",
                    (i + 1), t.getTransport().getOperatorName(),
                    t.getDepartureTime().format(Trip.TIME_FORMATTER),
                    t.getFormattedArrival(),
                    t.getFormattedDuration());
        }
        int tripIdx = readIntSafe("Select trip number", 1, trips.size()) - 1;
        Trip chosenTrip = trips.get(tripIdx);

        // 6. Choose Class
        System.out.println("\n" + ConsoleColors.colorize("--- STEP 4: SELECT TRAVEL CLASS ---", ConsoleColors.YELLOW, ConsoleColors.BOLD));
        List<SearchService.ClassFareInfo> classInfos = searchService.getClassesForTrip(chosenTrip);
        for (int i = 0; i < classInfos.size(); i++) {
            SearchService.ClassFareInfo cfi = classInfos.get(i);
            System.out.printf("  %d. %-18s | Starting Fare: " + Constants.CURRENCY + "%-5.0f | Vacant Seats: %d/%d\n",
                    (i + 1), cfi.getSeatClass().getDisplayName(), cfi.getStartingFare(),
                    cfi.getSeatsLeft(), cfi.getTotalSeats());
        }
        int classIdx = readIntSafe("Select class number", 1, classInfos.size()) - 1;
        SeatClass chosenClass = classInfos.get(classIdx).getSeatClass();
        SeatLayout chosenLayout = chosenTrip.getTransport().getSeatLayout(chosenClass);

        if (chosenLayout.getAvailableSeatsCount() == 0) {
            System.out.println(ConsoleColors.colorize("Sorry, this class is completely sold out. Please choose another class.", ConsoleColors.RED));
            return;
        }

        // 7. Show Seat Map and Seat Selection Loop (Rule 1)
        System.out.println("\n" + ConsoleColors.colorize("--- STEP 5: SEAT MAP & SEAT SELECTION ---", ConsoleColors.YELLOW, ConsoleColors.BOLD));
        SeatMapRenderer.renderLayout(chosenLayout);

        List<Seat> selectedSeats = promptSeatSelection(chosenLayout);
        if (selectedSeats.isEmpty()) {
            return;
        }

        // Temporarily mark selected seats with cyan status
        for (Seat s : selectedSeats) {
            s.setStatus(SeatStatus.SELECTED);
        }
        temporarilySelectedSeats = selectedSeats;

        // 8. Passenger Details
        System.out.println("\n" + ConsoleColors.colorize("--- STEP 6: PASSENGER DETAILS ---", ConsoleColors.YELLOW, ConsoleColors.BOLD));
        List<Passenger> passengers = promptPassengerDetails(selectedSeats);

        // 9. Fare Summary
        System.out.println("\n" + ConsoleColors.colorize("--- STEP 7: FARE BREAKDOWN & CONFIRMATION ---", ConsoleColors.YELLOW, ConsoleColors.BOLD));
        double totalTripFare = printFareSummary(chosenClass, route.getDistanceKm(), selectedSeats);

        boolean proceed = promptConfirmation("Confirm booking and proceed to payment?");
        if (!proceed) {
            // Rollback selection
            for (Seat s : selectedSeats) {
                s.setStatus(SeatStatus.AVAILABLE);
            }
            temporarilySelectedSeats = Collections.emptyList();
            System.out.println(ConsoleColors.colorize("Booking transaction cancelled. Seats released.", ConsoleColors.YELLOW));
            return;
        }

        // 10. Payment Gateway and Ticket Issuance
        Booking booking = bookingService.createBooking(chosenTrip, chosenClass, selectedSeats, passengers);
        Payment payment;
        double finalTotal;
        while (true) {
            System.out.println("\n" + ConsoleColors.colorize("--- STEP 8: PAYMENT GATEWAY ---", ConsoleColors.YELLOW, ConsoleColors.BOLD));
            PaymentMode paymentMode;
            try {
                paymentMode = promptPaymentMode();
            } catch (PaymentFailedException e) {
                printPaymentFailure("PAYMENT CANCELLED", e.getMessage());
                if (promptPaymentRecovery()) {
                    continue;
                }
                releaseSelectedSeats(selectedSeats);
                temporarilySelectedSeats = Collections.emptyList();
                return;
            }

            payment = PaymentFactory.createPayment(paymentMode);
            double paymentFee = payment.getProcessingFee(totalTripFare);
            finalTotal = Math.round(totalTripFare + paymentFee);
            printPaymentSummary(totalTripFare, paymentFee, finalTotal);
            booking.setTotalFare(finalTotal);
            booking.setPayment(payment);

            try {
                paymentService.processPayment(payment, finalTotal, scanner, booking.getPnr());
                break;
            } catch (PaymentFailedException e) {
                String heading = e.getMessage().toLowerCase().contains("cancel")
                        ? "PAYMENT CANCELLED"
                        : "PAYMENT FAILED";
                printPaymentFailure(heading, e.getMessage());
                if (!promptPaymentRecovery()) {
                    releaseSelectedSeats(selectedSeats);
                    temporarilySelectedSeats = Collections.emptyList();
                    return;
                }
            }
        }

        Ticket ticket = bookingService.confirmBooking(booking);
        temporarilySelectedSeats = Collections.emptyList();
        printPaymentSuccess(payment, finalTotal);
        ticket.printTicket();

        // Redraw updated seat map showing new booked crosses
        System.out.println(ConsoleColors.colorize("UPDATED SEAT MAP (New reservations marked as Sold / X):", ConsoleColors.CYAN, ConsoleColors.BOLD));
        SeatMapRenderer.renderLayout(chosenLayout);
        System.out.printf("Remaining Available Seats in %s: %d / %d\n",
                chosenClass.getDisplayName(), chosenLayout.getAvailableSeatsCount(), chosenLayout.getTotalSeats());
    }

    // -------------------------------------------------------------------------
    // Seat Selection Helper with Exception Retry (Phase 5 Rule 1)
    // -------------------------------------------------------------------------

    private List<Seat> promptSeatSelection(SeatLayout layout) {
        int attempts = 0;
        while (true) {
            System.out.print("\nEnter seat numbers separated by commas (max 6 seats, e.g. L1,L2 or 1A,1B): ");
            String input = readLineSafe();
            if (input.equalsIgnoreCase("cancel") || input.equalsIgnoreCase("exit")) {
                return Collections.emptyList();
            }

            String[] tokens = input.split(",");
            if (tokens.length == 0 || input.trim().isEmpty()) {
                System.out.println(ConsoleColors.colorize("Please enter at least one seat number.", ConsoleColors.RED));
                checkPromptAttempts(++attempts);
                continue;
            }
            if (tokens.length > 6) {
                System.out.println(ConsoleColors.colorize("You can book a maximum of 6 seats per transaction. Provided: " + tokens.length, ConsoleColors.RED));
                checkPromptAttempts(++attempts);
                continue;
            }

            List<Seat> validated = new ArrayList<>();
            boolean hasError = false;

            for (String rawToken : tokens) {
                String seatNum = rawToken.trim();
                if (seatNum.isEmpty()) continue;

                try {
                    Seat seat = bookingService.bookSeat(layout, seatNum);
                    if (validated.contains(seat)) {
                        System.out.println(ConsoleColors.colorize("Duplicate seat entered: " + seatNum, ConsoleColors.RED));
                        hasError = true;
                        break;
                    }
                    validated.add(seat);
                } catch (InvalidSeatException e) {
                    System.out.println(ConsoleColors.colorize("Error: " + e.getMessage(), ConsoleColors.RED));
                    hasError = true;
                    break;
                } catch (SeatAlreadyBookedException e) {
                    System.out.println(ConsoleColors.colorize("Error: " + e.getMessage(), ConsoleColors.RED));
                    hasError = true;
                    break;
                }
            }

            if (!hasError && !validated.isEmpty()) {
                return validated;
            }
            System.out.println(ConsoleColors.colorize("Please re-enter your seat selection.", ConsoleColors.YELLOW));
            checkPromptAttempts(++attempts);
        }
    }

    private List<Passenger> promptPassengerDetails(List<Seat> seats) {
        List<Passenger> list = new ArrayList<>();
        for (int i = 0; i < seats.size(); i++) {
            Seat s = seats.get(i);
            System.out.printf("\nDetails for Seat [%s]:\n", s.getSeatNumber());

            String name = promptNonEmpty("  Passenger Name");
            int age = readIntSafe("  Passenger Age", 1, 120);
            String gender = promptGender();

            list.add(new Passenger(name, age, gender, s.getSeatNumber()));
        }
        return list;
    }

    private double printFareSummary(SeatClass sc, double distanceKm, List<Seat> seats) {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("  Itemized Charges for %d x %s Seat(s):\n", seats.size(), sc.getDisplayName());
        double totalSum = 0.0;

        for (int i = 0; i < seats.size(); i++) {
            Seat s = seats.get(i);
            PricingService.FareBreakdown fb = PricingService.calculateFare(sc, distanceKm, s);
            System.out.printf("   * Seat %-4s: Base " + Constants.CURRENCY + "%.0f + Res/Airport " + Constants.CURRENCY + "%.0f + GST " + Constants.CURRENCY + "%.0f + Conv " + Constants.CURRENCY + "%.0f",
                    s.getSeatNumber(), fb.getBaseFare(), (fb.getReservationFee() + fb.getAirportFee()),
                    fb.getGstAmount(), fb.getConvenienceFee());
            if (fb.getSeatAddOn() > 0) {
                System.out.printf(" + Addon " + Constants.CURRENCY + "%.0f", fb.getSeatAddOn());
            }
            System.out.printf(" => " + Constants.CURRENCY + "%.0f\n", fb.getTotalFare());
            totalSum += fb.getTotalFare();
        }

        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("  FINAL AMOUNT DUE: " + ConsoleColors.colorize(Constants.CURRENCY + "%.0f", ConsoleColors.GREEN, ConsoleColors.BOLD) + "\n", totalSum);
        System.out.println("--------------------------------------------------------------------------------");
        return totalSum;
    }

    private PaymentMode promptPaymentMode() throws PaymentFailedException {
        PaymentMode[] paymentModes = PaymentMode.values();
        int attempts = 0;
        System.out.println("Choose payment method:");
        for (int i = 0; i < paymentModes.length; i++) {
            System.out.printf("  %d. %s%n", i + 1, paymentModes[i].getDisplayName());
        }

        while (true) {
            System.out.print("Select payment method (or type cancel): ");
            String input = readLineSafe();
            if ("cancel".equalsIgnoreCase(input)) {
                throw new PaymentFailedException("Payment was cancelled by the user.");
            }
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= paymentModes.length) {
                    return paymentModes[choice - 1];
                }
            } catch (NumberFormatException ignored) {
                // Retry with a clear message; no booking state has been committed.
            }
            System.out.println(ConsoleColors.colorize("Select a valid payment option.", ConsoleColors.RED));
            checkPromptAttempts(++attempts);
        }
    }

    private void printPaymentSummary(double fare, double paymentFee, double finalTotal) {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("  Booking fare       : %s%.0f%n", Constants.CURRENCY, fare);
        System.out.printf("  Payment-mode fee   : %s%.0f%n", Constants.CURRENCY, paymentFee);
        System.out.printf("  FINAL TOTAL        : %s%.0f%n", Constants.CURRENCY, finalTotal);
        System.out.println("--------------------------------------------------------------------------------");
    }

    private void printPaymentSuccess(Payment payment, double finalTotal) {
        System.out.println();
        List<String> lines = new ArrayList<>();
        if (payment.getPaymentMode() == PaymentMode.CASH) {
            lines.add("BOOKING CONFIRMED - PAY AT ARRIVAL");
            lines.add("Amount to pay: " + Constants.CURRENCY + String.format("%.0f", finalTotal));
        } else {
            lines.add("PAYMENT SUCCESSFUL");
            lines.add("Payment mode: " + payment.getPaymentMode().getDisplayName());
            lines.add("Amount paid: " + Constants.CURRENCY + String.format("%.0f", finalTotal));
        }
        lines.add("Transaction ID: " + payment.getTransactionId());
        lines.add("Thank you for booking with us!");
        printPaymentBox(lines, ConsoleColors.GREEN);
    }

    private void printPaymentFailure(String heading, String reason) {
        System.out.println();
        List<String> lines = new ArrayList<>();
        lines.add(heading);
        lines.add("Reason: " + reason);
        lines.add("No seats were booked.");
        printPaymentBox(lines, ConsoleColors.RED);
        System.out.println("  1. Try another payment method");
        System.out.println("  2. Return to the main menu");
    }

    private void printPaymentBox(List<String> lines, String color) {
        int width = 78;
        for (String line : lines) {
            width = Math.max(width, line.length() + 4);
        }
        String border = "+" + repeat('-', width - 2) + "+";
        System.out.println(ConsoleColors.colorize(border, color));
        for (String line : lines) {
            System.out.println(ConsoleColors.colorize(
                    String.format("| %-"+(width - 4)+"s |", line), color));
        }
        System.out.println(ConsoleColors.colorize(border, color));
    }

    private boolean promptPaymentRecovery() {
        int attempts = 0;
        while (true) {
            System.out.print("Select option [1-2]: ");
            String input = readLineSafe();
            if ("1".equals(input)) {
                return true;
            }
            if ("2".equals(input)) {
                return false;
            }
            System.out.println(ConsoleColors.colorize("Enter 1 to retry or 2 to return to the main menu.", ConsoleColors.RED));
            checkPromptAttempts(++attempts);
        }
    }

    private String repeat(char character, int count) {
        StringBuilder repeated = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            repeated.append(character);
        }
        return repeated.toString();
    }

    private void releaseSelectedSeats(List<Seat> seats) {
        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.AVAILABLE);
        }
    }

    // -------------------------------------------------------------------------
    // View and Cancel Flows
    // -------------------------------------------------------------------------

    private void executeViewTicket() {
        System.out.println("\n" + ConsoleColors.colorize("--- VIEW TICKET BY PNR ---", ConsoleColors.YELLOW, ConsoleColors.BOLD));
        System.out.print("Enter PNR number (for example TRV-10001 or 10001): ");
        String pnr = readLineSafe();
        Booking booking = bookingService.getBookingByPnr(pnr);
        if (booking == null) {
            System.out.println(ConsoleColors.colorize("No booking found with PNR: " + pnr, ConsoleColors.RED));
            return;
        }
        if (booking.getStatus() == BookingStatus.PENDING) {
            System.out.println(ConsoleColors.colorize("Booking " + pnr + " is not confirmed.", ConsoleColors.YELLOW));
            return;
        }
        printTicket(booking);
    }

    private void executeMyBookings() {
        List<Booking> bookings = getBookingsNewestFirst();
        if (bookings.isEmpty()) {
            System.out.println(ConsoleColors.colorize("No bookings yet", ConsoleColors.YELLOW));
            return;
        }

        while (true) {
            System.out.println("\n" + ConsoleColors.colorize("--- MY BOOKINGS ---", ConsoleColors.YELLOW, ConsoleColors.BOLD));
            printBookingsTable(bookings);
            System.out.println("  1. View a ticket");
            System.out.println("  2. Search by passenger name");
            System.out.println("  3. Back");
            int choice = readIntSafe("Select option", 1, 3);
            if (choice == 1) {
                Booking booking = chooseBookingFromList(bookings, "Choose booking number to view");
                if (booking != null) {
                    printTicket(booking);
                }
            } else if (choice == 2) {
                searchBookingsByPassengerName(bookings);
            } else {
                return;
            }
        }
    }

    private void searchBookingsByPassengerName(List<Booking> bookings) {
        String query = promptNonEmpty("Passenger name to search");
        String normalizedQuery = query.toLowerCase();
        List<Booking> matches = new ArrayList<>();
        for (Booking booking : bookings) {
            for (Passenger passenger : booking.getPassengers()) {
                if (passenger.getName().toLowerCase().contains(normalizedQuery)) {
                    matches.add(booking);
                    break;
                }
            }
        }
        if (matches.isEmpty()) {
            System.out.println(ConsoleColors.colorize("No bookings found for passenger name matching '" + query + "'.", ConsoleColors.YELLOW));
            return;
        }
        printBookingsTable(matches);
    }

    private void executeCancelTicket() {
        List<Booking> bookings = getBookingsNewestFirst();
        if (bookings.isEmpty()) {
            System.out.println(ConsoleColors.colorize("No bookings yet", ConsoleColors.YELLOW));
            return;
        }

        System.out.println("\n" + ConsoleColors.colorize("--- CANCEL TICKET ---", ConsoleColors.YELLOW, ConsoleColors.BOLD));
        System.out.println("  1. Choose from My Bookings");
        System.out.println("  2. Enter PNR");
        System.out.println("  3. Back");
        int choice = readIntSafe("Select option", 1, 3);
        Booking booking;
        if (choice == 1) {
            printBookingsTable(bookings);
            booking = chooseBookingFromList(bookings, "Choose booking number to cancel");
        } else if (choice == 2) {
            System.out.print("Enter PNR number to cancel: ");
            String pnr = readLineSafe();
            booking = bookingService.getBookingByPnr(pnr);
            if (booking == null) {
                System.out.println(ConsoleColors.colorize("No booking found with PNR: " + pnr, ConsoleColors.RED));
                return;
            }
        } else {
            return;
        }
        if (booking == null) {
            return;
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            System.out.println(ConsoleColors.colorize("Booking " + booking.getPnr() + " is already cancelled.", ConsoleColors.YELLOW));
            return;
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            System.out.println(ConsoleColors.colorize("Only confirmed bookings can be cancelled.", ConsoleColors.YELLOW));
            return;
        }

        printTicket(booking);
        RefundPolicy refundPolicy = booking.getPayment() != null
                && booking.getPayment().getStatus() == PaymentStatus.PAY_AT_ARRIVAL
                ? new CashRefundPolicy()
                : new TimeBasedRefundPolicy();
        int refundPercentage = refundPolicy.getRefundPercentage(booking);
        double refundAmount = Math.round(booking.getTotalFare() * refundPercentage / 100.0);
        System.out.printf("Refund amount: %s%.0f (%d%% of total fare)%n",
                Constants.CURRENCY, refundAmount, refundPercentage);

        if (!promptConfirmation("Confirm cancellation of " + booking.getPnr() + "?")) {
            System.out.println("Cancellation aborted.");
            return;
        }

        try {
            bookingService.cancelBooking(booking);
        } catch (IllegalStateException e) {
            System.out.println(ConsoleColors.colorize("Could not save cancellation: " + e.getMessage(), ConsoleColors.RED));
            return;
        }
        List<String> cancellationMessage = new ArrayList<>();
        cancellationMessage.add("TICKET CANCELLED");
        cancellationMessage.add("Refund amount: " + Constants.CURRENCY + String.format("%.0f", refundAmount));
        cancellationMessage.add("Refund percentage: " + refundPercentage + "%");
        printPaymentBox(cancellationMessage, refundAmount > 0.0 ? ConsoleColors.YELLOW : ConsoleColors.RED);
    }

    private List<Booking> getBookingsNewestFirst() {
        List<Booking> bookings = new ArrayList<>();
        for (Booking booking : bookingService.getAllBookings().values()) {
            if (booking.getStatus() != BookingStatus.PENDING) {
                bookings.add(booking);
            }
        }
        bookings.sort(Comparator.comparing(Booking::getBookingTime).reversed());
        return bookings;
    }

    private Booking chooseBookingFromList(List<Booking> bookings, String prompt) {
        if (bookings.isEmpty()) {
            return null;
        }
        int choice = readIntSafe(prompt, 1, bookings.size());
        return bookings.get(choice - 1);
    }

    private void printBookingsTable(List<Booking> bookings) {
        System.out.printf("%-4s %-13s %-34s %-12s %-18s %-14s %-22s %-12s %10s%n",
                "#", "PNR", "Route", "Travel Date", "Class", "Seats", "Passengers", "Status", "Total");
        System.out.println(repeat('-', 145));
        for (int i = 0; i < bookings.size(); i++) {
            Booking booking = bookings.get(i);
            Trip trip = booking.getTrip();
            String route = trip.getRoute().getSource().getName() + " -> "
                    + trip.getRoute().getDestination().getName();
            String passengers = formatPassengerNames(booking);
            System.out.printf("%-4d %-13s %-34s %-12s %-18s %-14s %-22s %-12s %s%.0f%n",
                    i + 1,
                    booking.getPnr(),
                    truncate(route, 34),
                    trip.getDate().format(Trip.DATE_FORMATTER),
                    truncate(booking.getSeatClass().getDisplayName(), 18),
                    truncate(String.join(", ", booking.getSeatNumbers()), 14),
                    truncate(passengers, 22),
                    booking.getStatus(),
                    Constants.CURRENCY,
                    booking.getTotalFare());
        }
    }

    private String formatPassengerNames(Booking booking) {
        List<String> names = new ArrayList<>();
        for (Passenger passenger : booking.getPassengers()) {
            names.add(passenger.getName() + " (" + passenger.getAge() + ")");
        }
        return String.join(", ", names);
    }

    private String truncate(String value, int width) {
        if (value.length() <= width) {
            return value;
        }
        return value.substring(0, width - 3) + "...";
    }

    private void printTicket(Booking booking) {
        new Ticket(booking).printTicket();
    }

    // -------------------------------------------------------------------------
    // Input Prompts & Validation
    // -------------------------------------------------------------------------

    private City promptCitySelection(String label) {
        int attempts = 0;
        while (true) {
            System.out.printf("Enter %s city name or prefix (e.g. Mum, Hyd, Delhi): ", label);
            String input = readLineSafe();
            if (input.equalsIgnoreCase("cancel") || input.equalsIgnoreCase("exit")) return null;

            City exact = cityRepository.getCityByName(input);
            if (exact != null) return exact;

            List<City> matches = cityRepository.searchByNamePrefix(input);
            if (matches.isEmpty()) {
                System.out.println(ConsoleColors.colorize("No city found matching '" + input + "'. Try again.", ConsoleColors.RED));
                checkPromptAttempts(++attempts);
                continue;
            }
            if (matches.size() == 1) {
                return matches.get(0);
            }

            System.out.println("Multiple cities found:");
            for (int i = 0; i < matches.size(); i++) {
                System.out.printf("  %d. %s\n", (i + 1), matches.get(i).toString());
            }
            int pick = readIntSafe("Select city number", 1, matches.size()) - 1;
            return matches.get(pick);
        }
    }

    private LocalDate promptTravelDate() {
        int attempts = 0;
        while (true) {
            System.out.print("Enter travel date (dd-MM-yyyy): ");
            String str = readLineSafe();
            if (str.equalsIgnoreCase("cancel")) return null;
            try {
                LocalDate date = LocalDate.parse(str.trim(), Trip.DATE_FORMATTER);
                LocalDate today = LocalDate.now();
                if (date.isBefore(today)) {
                    System.out.println(ConsoleColors.colorize("Travel date cannot be in the past.", ConsoleColors.RED));
                    checkPromptAttempts(++attempts);
                    continue;
                }
                return date;
            } catch (java.time.format.DateTimeParseException e) {
                System.out.println(ConsoleColors.colorize("Invalid format. Expected dd-MM-yyyy (e.g. 05-11-2026).", ConsoleColors.RED));
                checkPromptAttempts(++attempts);
            }
        }
    }

    private String promptGender() {
        int attempts = 0;
        while (true) {
            System.out.print("  Passenger Gender (M/F/O): ");
            String g = readLineSafe().toUpperCase();
            if (g.equals("M") || g.startsWith("MALE")) return "Male";
            if (g.equals("F") || g.startsWith("FEMALE")) return "Female";
            if (g.equals("O") || g.startsWith("OTHER")) return "Other";
            System.out.println(ConsoleColors.colorize("Please enter M, F, or O.", ConsoleColors.RED));
            checkPromptAttempts(++attempts);
        }
    }

    private String promptNonEmpty(String prompt) {
        int attempts = 0;
        while (true) {
            System.out.print(prompt + ": ");
            String val = readLineSafe();
            if (!val.isEmpty()) return val;
            System.out.println(ConsoleColors.colorize("Value cannot be blank.", ConsoleColors.RED));
            checkPromptAttempts(++attempts);
        }
    }

    private boolean promptConfirmation(String prompt) {
        int attempts = 0;
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String resp = readLineSafe().toLowerCase();
            if (resp.equals("y") || resp.equals("yes")) return true;
            if (resp.equals("n") || resp.equals("no")) return false;
            System.out.println(ConsoleColors.colorize("Please respond with 'y' or 'n'.", ConsoleColors.RED));
            checkPromptAttempts(++attempts);
        }
    }

    private int readIntSafe(String prompt, int min, int max) {
        int attempts = 0;
        while (true) {
            System.out.printf("%s [%d-%d]: ", prompt, min, max);
            String line = readLineSafe();
            try {
                int val = Integer.parseInt(line);
                if (val >= min && val <= max) return val;
                System.out.printf(ConsoleColors.colorize("Number must be between %d and %d.\n", ConsoleColors.RED), min, max);
                checkPromptAttempts(++attempts);
            } catch (NumberFormatException e) {
                System.out.println(ConsoleColors.colorize("Invalid integer. Please enter digits only.", ConsoleColors.RED));
                checkPromptAttempts(++attempts);
            }
        }
    }

    private String readLineSafe() {
        return InputHelper.readLine(scanner, null).trim();
    }

    private void checkPromptAttempts(int attempts) {
        if (attempts >= MAX_INVALID_ATTEMPTS) {
            throw new TooManyInvalidAttemptsException();
        }
    }
}
