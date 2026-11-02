package com.travel;

import com.travel.enums.SeatClass;
import com.travel.enums.SeatStatus;
import com.travel.enums.TransportType;
import com.travel.layout.BerthBayLayout;
import com.travel.layout.CabinLayout;
import com.travel.layout.ChairLayout;
import com.travel.layout.FlightCabinLayout;
import com.travel.layout.SeatLayout;
import com.travel.layout.SleeperDeckLayout;
import com.travel.model.City;
import com.travel.model.Route;
import com.travel.model.Trip;
import com.travel.exception.InputEndedException;
import com.travel.exception.TooManyInvalidAttemptsException;
import com.travel.repository.CityRepository;
import com.travel.seat.Seat;
import com.travel.service.BookingService;
import com.travel.service.PricingService;
import com.travel.service.SearchService;
import com.travel.service.TripGenerator;
import com.travel.transport.Train;
import com.travel.ui.ConsoleMenu;
import com.travel.ui.SeatMapRenderer;
import com.travel.ui.BigText;
import com.travel.ui.ScreenStyle;
import com.travel.util.ConsoleColors;
import com.travel.util.Constants;
import com.travel.util.Clock;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Demonstrates: Program Entry Point, Command-Line Parameter Dispatching,
 * Integration of Repository, Service, Model, and UI components.
 */
public class Main {

    private static final long SEED = 42L;
    private static final double BOOKED_RATIO = 0.35;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static void main(String[] args) {
        // Force UTF-8 on stdout and stderr so ₹ renders correctly on Windows
        try {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
        } catch (Exception ignored) {}

        try {
            run(args);
        } catch (InputEndedException e) {
            System.out.println(ConsoleColors.colorize("Input ended. Exiting.", ConsoleColors.YELLOW));
        } catch (TooManyInvalidAttemptsException e) {
            System.out.println(ConsoleColors.colorize(
                    "Too many invalid attempts. Returning to the main menu.",
                    ConsoleColors.YELLOW
            ));
        }
    }

    private static void run(String[] args) {
        if (args.length == 0) {
            printUsageHelp();
            return;
        }

        for (int i = 0; i < args.length; i++) {
            if ("--now".equalsIgnoreCase(args[i])) {
                if (i + 1 >= args.length) {
                    System.out.println("Usage: --now \"dd-MM-yyyy HH:mm\"");
                    return;
                }
                try {
                    Clock.setFixedTime(args[++i]);
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                    return;
                }
            } else if ("--ascii".equalsIgnoreCase(args[i])) {
                ConsoleColors.setAsciiMode(true);
            } else if ("--nocolor".equalsIgnoreCase(args[i])) {
                ConsoleColors.setNoColor(true);
            }
        }

        for (int i = 0; i < args.length; i++) {
            String arg = args[i].trim();
            if ("--now".equalsIgnoreCase(arg)) {
                i++;
            } else if ("--ascii".equalsIgnoreCase(arg)) {
                ConsoleColors.setAsciiMode(true);
            } else if ("--nocolor".equalsIgnoreCase(arg)) {
                ConsoleColors.setNoColor(true);
            } else if ("--menu".equalsIgnoreCase(arg)) {
                launchInteractiveMenu();
                return;
            } else if ("--test".equalsIgnoreCase(arg)) {
                if (!TestRunner.run()) {
                    System.exit(1);
                }
                return;
            } else if ("--cities".equalsIgnoreCase(arg)) {
                printCitiesList();
                return;
            } else if ("--fares".equalsIgnoreCase(arg)) {
                if (i + 3 < args.length) {
                    String src = args[++i].trim();
                    String dst = args[++i].trim();
                    String dateStr = args[++i].trim();
                    printFaresTable(src, dst, dateStr);
                } else {
                    System.out.println("Usage: --fares <source> <destination> <dd-MM-yyyy>");
                }
                return;
            } else if ("--trips".equalsIgnoreCase(arg)) {
                if (i + 4 < args.length) {
                    String src = args[++i].trim();
                    String dst = args[++i].trim();
                    String modeStr = args[++i].trim();
                    String dateStr = args[++i].trim();
                    searchAndPrintTrips(src, dst, modeStr, dateStr);
                } else {
                    System.out.println("Usage: --trips <source> <destination> <mode> <dd-MM-yyyy>");
                }
                return;
            } else if ("--demo".equalsIgnoreCase(arg)) {
                String filterName = null;
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    filterName = args[++i].trim();
                }
                runDemo(filterName);
                return;
            }
        }

        printUsageHelp();
    }

    private static void printUsageHelp() {
        List<String> lines = new ArrayList<>();
        lines.add(ConsoleColors.colorize(
                ScreenStyle.center("TRAVEL TICKET BOOKING SYSTEM - HELP", 74),
                ConsoleColors.CYAN, ConsoleColors.BOLD));
        lines.add(ScreenStyle.ruleContent());
        lines.add("Usage:");
        lines.add("  --menu                 : Starts the interactive booking menu");
        lines.add("  --cities               : Lists all destinations grouped by tier");
        lines.add("  --fares <src> <dst> <date> : Prints the fare table");
        lines.add("  --trips <src> <dst> <mode> <date> : Displays scheduled trips");
        lines.add("  --now <date and time>  : Overrides the current time");
        lines.add("  --test                 : Runs the non-interactive test suite");
        lines.add("  --demo [class]         : Shows a non-interactive seat map");
        lines.add("  --ascii                : Enables ASCII borders and banner");
        lines.add("  --nocolor              : Disables ANSI color codes");
        printFrame(lines);
    }

    private static void launchInteractiveMenu() {
        BigText.printBanner();
        CityRepository cityRepo = CityRepository.getInstance();
        SearchService searchService = new SearchService();
        BookingService bookingService = new BookingService();
        ConsoleMenu menu = new ConsoleMenu(cityRepo, searchService, bookingService);
        menu.start();
    }

    // -------------------------------------------------------------------------
    // Phase 4: Fares Table Command
    // -------------------------------------------------------------------------

    private static void printFaresTable(String srcName, String dstName, String dateStr) {
        CityRepository repo = CityRepository.getInstance();
        City source = repo.getCityByName(srcName);
        City destination = repo.getCityByName(dstName);

        if (source == null) {
            System.out.printf("Error: Source city '%s' not found.\n", srcName);
            return;
        }
        if (destination == null) {
            System.out.printf("Error: Destination city '%s' not found.\n", dstName);
            return;
        }

        List<String> lines = new ArrayList<>();
        lines.add(ConsoleColors.colorize(
                "FARE COMPARISON: " + source.getName().toUpperCase() + " -> "
                        + destination.getName().toUpperCase() + " (" + dateStr + ")",
                ConsoleColors.CYAN, ConsoleColors.BOLD));
        lines.add(ScreenStyle.ruleContent());
        lines.add(String.format(
                "%-6s | %-12s | %-7s | %-7s | %-6s | %-7s | %-7s",
                "Mode", "Class", "Base", "Res/Air", "GST", "Fee", "Total"));

        for (TransportType mode : TransportType.values()) {
            Route route = new Route(source, destination, mode);
            if (!route.isOperable()) {
                lines.add(mode.getDisplayName() + " unavailable: "
                        + route.getInoperableReason());
                continue;
            }

            double dist = route.getDistanceKm();
            for (SeatClass sc : SeatClass.values()) {
                if (sc.getTransportType() != mode) continue;

                // Rule 4b: For routes under 250 km, offer only Chair Car and Non-AC Sleeper on trains
                if (mode == TransportType.TRAIN && dist < Train.SHORT_ROUTE_THRESHOLD_KM) {
                    if (sc == SeatClass.TRAIN_AC_2_TIER || sc == SeatClass.TRAIN_AC_1_TIER) {
                        continue;
                    }
                }

                PricingService.FareBreakdown fb = PricingService.calculateFare(sc, dist, null);
                double resOrAir = fb.getReservationFee() + fb.getAirportFee();

                String row = String.format(
                        "%-6.6s | %-12.12s | %-7s | %-7s | %-6s | %-7s | ",
                        mode.getDisplayName(), sc.getDisplayName(),
                        Constants.CURRENCY + String.format("%.0f", fb.getBaseFare()),
                        Constants.CURRENCY + String.format("%.0f", resOrAir),
                        Constants.CURRENCY + String.format("%.0f", fb.getGstAmount()),
                        Constants.CURRENCY + String.format("%.0f", fb.getConvenienceFee()));
                row += ConsoleColors.colorize(
                        String.format("%-7s",
                                Constants.CURRENCY + String.format("%.0f", fb.getTotalFare())),
                        sc.isPremium() ? ConsoleColors.COLOR_PREMIUM
                                : ConsoleColors.COLOR_STANDARD,
                        ConsoleColors.BOLD);
                lines.add(row);
            }
        }
        printFrame(lines);
    }

    // -------------------------------------------------------------------------
    // Phase 3: Cities List grouped by tier
    // -------------------------------------------------------------------------

    private static void printCitiesList() {
        CityRepository repo = CityRepository.getInstance();
        List<City> all = repo.getAllCities();

        List<String> lines = new ArrayList<>();
        lines.add(ConsoleColors.colorize(
                ScreenStyle.center("ALL DESTINATIONS GROUPED BY TIER", 74),
                ConsoleColors.CYAN, ConsoleColors.BOLD));
        lines.add(String.format("Total Cities Loaded: %d", all.size()));

        String[] tiers = {"Tier 1", "Tier 2", "Tourist"};
        for (String tier : tiers) {
            List<City> tierCities = repo.getCitiesByTier(tier);
            lines.add(ScreenStyle.ruleContent());
            lines.add(ConsoleColors.colorize(
                    tier.toUpperCase() + " (" + tierCities.size() + " CITIES)",
                    ConsoleColors.YELLOW, ConsoleColors.BOLD));

            for (City c : tierCities) {
                String connectivity = String.format("Airport: %-3s | Station: %-3s",
                        (c.hasAirport() ? "Yes" : "No"), (c.hasStation() ? "Yes" : "No"));
                String places = String.join(", ", c.getTouristPlaces());

                lines.add(String.format("  [%02d] %-18s (%-16s) | %s",
                        c.getId(), c.getName(), c.getState(), connectivity));
                lines.add("  Tourist Places: " + places);
            }
        }
        printFrame(lines);
    }

    // -------------------------------------------------------------------------
    // Phase 3: Search and Print Trips
    // -------------------------------------------------------------------------

    private static void searchAndPrintTrips(String srcName, String dstName, String modeStr, String dateStr) {
        CityRepository repo = CityRepository.getInstance();
        City source = repo.getCityByName(srcName);
        City destination = repo.getCityByName(dstName);

        if (source == null) {
            System.out.printf("Error: Source city '%s' not found in database.\n", srcName);
            return;
        }
        if (destination == null) {
            System.out.printf("Error: Destination city '%s' not found in database.\n", dstName);
            return;
        }

        TransportType mode;
        try {
            mode = TransportType.valueOf(modeStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.printf("Error: Invalid transport mode '%s'. Valid options: bus, train, flight.\n", modeStr);
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            System.out.printf("Error: Invalid date format '%s'. Expected: dd-MM-yyyy (e.g. 05-11-2026).\n", dateStr);
            return;
        }

        Route route = new Route(source, destination, mode);

        if (!route.isOperable()) {
            printFrame(ConsoleColors.colorize("TRANSPORT MODE UNAVAILABLE",
                    ConsoleColors.RED, ConsoleColors.BOLD),
                    "Route  : " + source.getName() + " -> " + destination.getName(),
                    "Mode   : " + mode.getDisplayName(),
                    "Reason : " + route.getInoperableReason());
            return;
        }

        List<Trip> trips = TripGenerator.generateTrips(route, date);
        printFrame(
                ConsoleColors.colorize(
                        "TRIPS FOUND: " + source.getName().toUpperCase() + " -> "
                                + destination.getName().toUpperCase() + " ("
                                + mode.getDisplayName().toUpperCase() + ")",
                        ConsoleColors.CYAN, ConsoleColors.BOLD),
                String.format("Date: %s | Distance: %.1f km | Direct: %.1f km",
                        date.format(DATE_FORMATTER), route.getDistanceKm(),
                        route.getDirectDistanceKm()),
                "Trips Available: " + trips.size());

        for (int i = 0; i < trips.size(); i++) {
            Trip trip = trips.get(i);
            List<String> tripLines = new ArrayList<>();
            tripLines.add(String.format("Trip #%d: %s [%s]", i + 1,
                    trip.getTransport().getOperatorName(), trip.getTripId()));
            tripLines.add(String.format("Departure: %s | Arrival: %s | Duration: %s",
                    trip.getDepartureTime().format(Trip.TIME_FORMATTER),
                    trip.getFormattedArrival(), trip.getFormattedDuration()));

            StringBuilder sb = new StringBuilder();
            for (Map.Entry<SeatClass, SeatLayout> entry : trip.getTransport().getSeatLayouts().entrySet()) {
                SeatClass sc = entry.getKey();
                SeatLayout layout = entry.getValue();
                if (sb.length() > 0) sb.append(" | ");
                sb.append(sc.getDisplayName()).append(": ")
                  .append(layout.getAvailableSeatsCount()).append("/").append(layout.getTotalSeats());
            }
            tripLines.add("Seats Left: " + sb);
            printFrame(tripLines);
        }
    }

    private static void printFrame(String... lines) {
        for (String line : ScreenStyle.frame(lines)) {
            System.out.println(line);
        }
    }

    private static void printFrame(List<String> lines) {
        for (String line : ScreenStyle.frame(lines)) {
            System.out.println(line);
        }
    }

    // -------------------------------------------------------------------------
    // Phase 2 Demo Workflow (Preserved)
    // -------------------------------------------------------------------------

    private static void runDemo(String filterName) {
        List<SeatLayout> layouts = createAllLayouts();

        if (filterName != null && !filterName.isEmpty()) {
            String target = filterName.toLowerCase();
            SeatLayout matched = null;

            for (SeatLayout layout : layouts) {
                if (layout.getSeatClass().toString().equalsIgnoreCase(target)
                        || layout.getSeatClass().getDisplayName().equalsIgnoreCase(target)
                        || layout.getSeatClass().name().equalsIgnoreCase(target)) {
                    matched = layout;
                    break;
                }
            }

            if (matched == null) {
                for (SeatLayout layout : layouts) {
                    if (layout.getSeatClass().toString().toLowerCase().contains(target)) {
                        matched = layout;
                        break;
                    }
                }
            }

            if (matched != null) {
                SeatMapRenderer.renderLayout(matched);
            } else {
                System.out.println("No matching seat class for: " + filterName);
            }
        } else {
            for (SeatLayout layout : layouts) {
                SeatMapRenderer.renderLayout(layout);
            }
        }
    }

    public static List<SeatLayout> createAllLayouts() {
        List<SeatLayout> list = new ArrayList<>();
        long seedOffset = 0;

        SeatLayout busSeater = new ChairLayout(SeatClass.BUS_SEATER, 10, 2, 2, new char[]{'A', 'B', 'C', 'D'});
        seedBookedSeats(busSeater, SEED + seedOffset++);
        list.add(busSeater);

        SeatLayout busAcSeater = new ChairLayout(SeatClass.BUS_AC_SEATER, 10, 2, 2, new char[]{'A', 'B', 'C', 'D'});
        seedBookedSeats(busAcSeater, SEED + seedOffset++);
        list.add(busAcSeater);

        SeatLayout busSleeper = new SleeperDeckLayout(SeatClass.BUS_SLEEPER);
        seedBookedSeats(busSleeper, SEED + seedOffset++);
        list.add(busSleeper);

        SeatLayout busAcSleeper = new SleeperDeckLayout(SeatClass.BUS_AC_SLEEPER);
        seedBookedSeats(busAcSleeper, SEED + seedOffset++);
        list.add(busAcSleeper);

        SeatLayout trainChair = new ChairLayout(SeatClass.TRAIN_CHAIR_CAR, 10, 3, 2, new char[]{'A', 'B', 'C', 'D', 'E'});
        seedBookedSeats(trainChair, SEED + seedOffset++);
        list.add(trainChair);

        SeatLayout trainSleeper = new BerthBayLayout(SeatClass.TRAIN_SLEEPER, 8);
        seedBookedSeats(trainSleeper, SEED + seedOffset++);
        list.add(trainSleeper);

        SeatLayout train2Tier = new BerthBayLayout(SeatClass.TRAIN_AC_2_TIER, 6);
        seedBookedSeats(train2Tier, SEED + seedOffset++);
        list.add(train2Tier);

        SeatLayout train1Tier = new CabinLayout(SeatClass.TRAIN_AC_1_TIER);
        seedBookedSeats(train1Tier, SEED + seedOffset++);
        list.add(train1Tier);

        SeatLayout flightEcon = new FlightCabinLayout(SeatClass.FLIGHT_ECONOMY);
        seedBookedSeats(flightEcon, SEED + seedOffset++);
        list.add(flightEcon);

        SeatLayout flightBus = new FlightCabinLayout(SeatClass.FLIGHT_BUSINESS);
        seedBookedSeats(flightBus, SEED + seedOffset++);
        list.add(flightBus);

        return list;
    }

    private static void seedBookedSeats(SeatLayout layout, long seed) {
        Random rng = new Random(seed);
        for (Seat seat : layout.getAllSeats()) {
            if (rng.nextDouble() < BOOKED_RATIO) {
                seat.setStatus(SeatStatus.BOOKED);
            }
        }
    }
}
