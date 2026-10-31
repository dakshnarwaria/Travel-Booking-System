package com.travel.ui;

import com.travel.enums.BookingStatus;
import com.travel.enums.SeatClass;
import com.travel.enums.SeatStatus;
import com.travel.enums.TransportType;
import com.travel.exception.InvalidSeatException;
import com.travel.exception.SeatAlreadyBookedException;
import com.travel.layout.SeatLayout;
import com.travel.model.Booking;
import com.travel.model.City;
import com.travel.model.Passenger;
import com.travel.model.Route;
import com.travel.model.Ticket;
import com.travel.model.Trip;
import com.travel.repository.CityRepository;
import com.travel.seat.Seat;
import com.travel.service.BookingService;
import com.travel.service.PricingService;
import com.travel.service.SearchService;
import com.travel.util.ConsoleColors;
import com.travel.util.InputHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
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
    private final Scanner scanner;

    public ConsoleMenu(CityRepository cityRepository, SearchService searchService, BookingService bookingService) {
        this.cityRepository = cityRepository;
        this.searchService = searchService;
        this.bookingService = bookingService;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Entry point for the main application loop.
     */
    public void start() {
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readIntSafe("Select option", 1, 4);

            switch (choice) {
                case 1:
                    executeBookingFlow();
                    break;
                case 2:
                    executeViewTicket();
                    break;
                case 3:
                    executeCancelTicket();
                    break;
                case 4:
                    System.out.println(ConsoleColors.colorize("Thank you for using the Travel Ticket Booking System. Goodbye!", ConsoleColors.CYAN));
                    running = false;
                    break;
            }
        }
    }

    private void printMainMenu() {
        System.out.println("\n================================================================================");
        System.out.println(ConsoleColors.colorize("             TRAVEL TICKET BOOKING SYSTEM - MAIN MENU", ConsoleColors.CYAN, ConsoleColors.BOLD));
        System.out.println("================================================================================");
        System.out.println("  1. Book Travel Ticket");
        System.out.println("  2. View Ticket by PNR");
        System.out.println("  3. Cancel Ticket");
        System.out.println("  4. Exit System");
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
            System.out.printf("  %d. %-18s | Starting Fare: ₹%-5.0f | Vacant Seats: %d/%d\n",
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
            System.out.println(ConsoleColors.colorize("Booking transaction cancelled. Seats released.", ConsoleColors.YELLOW));
            return;
        }

        // 10. Payment Gateway (Stubbed for Phase 5)
        System.out.println("\n" + ConsoleColors.colorize("--- STEP 8: PAYMENT GATEWAY (SIMULATED) ---", ConsoleColors.YELLOW, ConsoleColors.BOLD));
        System.out.println("Processing transaction of ₹" + Math.round(totalTripFare) + " ... [SUCCESS]");

        // 11. Confirmation & Ticket Issuance
        Booking booking = bookingService.createBooking(chosenTrip, chosenClass, selectedSeats, passengers);
        Ticket ticket = bookingService.confirmBooking(booking);

        System.out.println("\n" + ConsoleColors.colorize("BOOKING CONFIRMED SUCCESSFULLY!", ConsoleColors.GREEN, ConsoleColors.BOLD));
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
        while (true) {
            System.out.print("\nEnter seat numbers separated by commas (max 6 seats, e.g. L1,L2 or 1A,1B): ");
            String input = readLineSafe();
            if (input.equalsIgnoreCase("cancel") || input.equalsIgnoreCase("exit")) {
                return Collections.emptyList();
            }

            String[] tokens = input.split(",");
            if (tokens.length == 0 || input.trim().isEmpty()) {
                System.out.println(ConsoleColors.colorize("Please enter at least one seat number.", ConsoleColors.RED));
                continue;
            }
            if (tokens.length > 6) {
                System.out.println(ConsoleColors.colorize("You can book a maximum of 6 seats per transaction. Provided: " + tokens.length, ConsoleColors.RED));
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
            System.out.printf("   * Seat %-4s: Base ₹%.0f + Res/Airport ₹%.0f + GST ₹%.0f + Conv ₹%.0f",
                    s.getSeatNumber(), fb.getBaseFare(), (fb.getReservationFee() + fb.getAirportFee()),
                    fb.getGstAmount(), fb.getConvenienceFee());
            if (fb.getSeatAddOn() > 0) {
                System.out.printf(" + Addon ₹%.0f", fb.getSeatAddOn());
            }
            System.out.printf(" => ₹%.0f\n", fb.getTotalFare());
            totalSum += fb.getTotalFare();
        }

        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("  FINAL AMOUNT DUE: " + ConsoleColors.colorize("₹%.0f", ConsoleColors.GREEN, ConsoleColors.BOLD) + "\n", totalSum);
        System.out.println("--------------------------------------------------------------------------------");
        return totalSum;
    }

    // -------------------------------------------------------------------------
    // View and Cancel Flows
    // -------------------------------------------------------------------------

    private void executeViewTicket() {
        System.out.println("\n" + ConsoleColors.colorize("--- VIEW TICKET BY PNR ---", ConsoleColors.YELLOW, ConsoleColors.BOLD));
        System.out.print("Enter PNR number: ");
        String pnr = readLineSafe();
        Booking booking = bookingService.getBookingByPnr(pnr);
        if (booking == null) {
            System.out.println(ConsoleColors.colorize("No booking found with PNR: " + pnr, ConsoleColors.RED));
            return;
        }
        Ticket ticket = new Ticket(booking);
        ticket.printTicket();
    }

    private void executeCancelTicket() {
        System.out.println("\n" + ConsoleColors.colorize("--- CANCEL TICKET ---", ConsoleColors.YELLOW, ConsoleColors.BOLD));
        System.out.print("Enter PNR number to cancel: ");
        String pnr = readLineSafe();
        Booking booking = bookingService.getBookingByPnr(pnr);
        if (booking == null) {
            System.out.println(ConsoleColors.colorize("No booking found with PNR: " + pnr, ConsoleColors.RED));
            return;
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            System.out.println(ConsoleColors.colorize("Booking is already cancelled.", ConsoleColors.YELLOW));
            return;
        }

        boolean confirm = promptConfirmation("Are you sure you want to cancel booking " + pnr + "?");
        if (confirm) {
            booking.setStatus(BookingStatus.CANCELLED);
            for (Seat seat : booking.getSeats()) {
                seat.cancel();
            }
            System.out.println(ConsoleColors.colorize("Ticket " + pnr + " successfully cancelled. Refund processed as per cancellation policy.", ConsoleColors.GREEN));
        } else {
            System.out.println("Cancellation aborted.");
        }
    }

    // -------------------------------------------------------------------------
    // Input Prompts & Validation
    // -------------------------------------------------------------------------

    private City promptCitySelection(String label) {
        while (true) {
            System.out.printf("Enter %s city name or prefix (e.g. Mum, Hyd, Delhi): ", label);
            String input = readLineSafe();
            if (input.equalsIgnoreCase("cancel") || input.equalsIgnoreCase("exit")) return null;

            City exact = cityRepository.getCityByName(input);
            if (exact != null) return exact;

            List<City> matches = cityRepository.searchByNamePrefix(input);
            if (matches.isEmpty()) {
                System.out.println(ConsoleColors.colorize("No city found matching '" + input + "'. Try again.", ConsoleColors.RED));
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
        while (true) {
            System.out.print("Enter travel date (dd-MM-yyyy): ");
            String str = readLineSafe();
            if (str.equalsIgnoreCase("cancel")) return null;
            try {
                LocalDate date = LocalDate.parse(str.trim(), Trip.DATE_FORMATTER);
                LocalDate today = LocalDate.now();
                if (date.isBefore(today)) {
                    System.out.println(ConsoleColors.colorize("Travel date cannot be in the past.", ConsoleColors.RED));
                    continue;
                }
                return date;
            } catch (Exception e) {
                System.out.println(ConsoleColors.colorize("Invalid format. Expected dd-MM-yyyy (e.g. 05-11-2026).", ConsoleColors.RED));
            }
        }
    }

    private String promptGender() {
        while (true) {
            System.out.print("  Passenger Gender (M/F/O): ");
            String g = readLineSafe().toUpperCase();
            if (g.equals("M") || g.startsWith("MALE")) return "Male";
            if (g.equals("F") || g.startsWith("FEMALE")) return "Female";
            if (g.equals("O") || g.startsWith("OTHER")) return "Other";
            System.out.println(ConsoleColors.colorize("Please enter M, F, or O.", ConsoleColors.RED));
        }
    }

    private String promptNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            String val = readLineSafe();
            if (!val.isEmpty()) return val;
            System.out.println(ConsoleColors.colorize("Value cannot be blank.", ConsoleColors.RED));
        }
    }

    private boolean promptConfirmation(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String resp = readLineSafe().toLowerCase();
            if (resp.equals("y") || resp.equals("yes")) return true;
            if (resp.equals("n") || resp.equals("no")) return false;
            System.out.println(ConsoleColors.colorize("Please respond with 'y' or 'n'.", ConsoleColors.RED));
        }
    }

    private int readIntSafe(String prompt, int min, int max) {
        while (true) {
            System.out.printf("%s [%d-%d]: ", prompt, min, max);
            String line = readLineSafe();
            try {
                int val = Integer.parseInt(line);
                if (val >= min && val <= max) return val;
                System.out.printf(ConsoleColors.colorize("Number must be between %d and %d.\n", ConsoleColors.RED), min, max);
            } catch (NumberFormatException e) {
                System.out.println(ConsoleColors.colorize("Invalid integer. Please enter digits only.", ConsoleColors.RED));
            }
        }
    }

    private String readLineSafe() {
        if (scanner.hasNextLine()) {
            return scanner.nextLine().trim();
        }
        return "";
    }
}
