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
import com.travel.repository.CityRepository;
import com.travel.seat.Seat;
import com.travel.service.BookingService;
import com.travel.service.PricingService;
import com.travel.service.SearchService;
import com.travel.service.TripGenerator;
import com.travel.transport.Train;
import com.travel.ui.ConsoleMenu;
import com.travel.ui.SeatMapRenderer;
import com.travel.util.ConsoleColors;
import com.travel.util.Constants;

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

        if (args.length == 0) {
            printUsageHelp();
            return;
        }

        for (int i = 0; i < args.length; i++) {
            String arg = args[i].trim();
            if ("--ascii".equalsIgnoreCase(arg)) {
                ConsoleColors.setAsciiMode(true);
            } else if ("--nocolor".equalsIgnoreCase(arg)) {
                ConsoleColors.setNoColor(true);
            } else if ("--menu".equalsIgnoreCase(arg)) {
                launchInteractiveMenu();
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
        System.out.println("================================================================================");
        System.out.println(ConsoleColors.colorize("             TRAVEL TICKET BOOKING SYSTEM - COMMAND LINE HELP", ConsoleColors.CYAN, ConsoleColors.BOLD));
        System.out.println("================================================================================");
        System.out.println("Usage:");
        System.out.println("  --menu                                  : Starts interactive console booking menu");
        System.out.println("  --cities                                : Lists all 50 cities grouped by tier");
        System.out.println("  --fares <src> <dst> <dd-MM-yyyy>        : Prints comprehensive fare table for route");
        System.out.println("  --trips <src> <dst> <mode> <dd-MM-yyyy> : Displays scheduled trips for date");
        System.out.println("  --demo [\"Class Name\"]                   : Non-interactive seat map preview");
        System.out.println("  --ascii                                 : Enables ASCII fallback borders");
        System.out.println("  --nocolor                               : Disables ANSI color codes");
        System.out.println("================================================================================");
    }

    private static void launchInteractiveMenu() {
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

        System.out.println("=========================================================================================");
        System.out.println(ConsoleColors.colorize(
                String.format("FARE COMPARISON SCHEDULE: %s -> %s (DATE: %s)", source.getName().toUpperCase(), destination.getName().toUpperCase(), dateStr),
                ConsoleColors.CYAN, ConsoleColors.BOLD
        ));
        System.out.println("=========================================================================================");
        System.out.printf("%-8s | %-16s | %-10s | %-12s | %-8s | %-12s | %-10s\n",
                "Mode", "Class", "Base Fare", "Res/Airport", "GST", "Convenience", "Total Fare");
        System.out.println("-----------------------------------------------------------------------------------------");

        for (TransportType mode : TransportType.values()) {
            Route route = new Route(source, destination, mode);
            if (!route.isOperable()) {
                System.out.printf("%-8s | UNAVAILABLE: %s\n", mode.getDisplayName(), route.getInoperableReason());
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

                System.out.printf("%-8s | %-16s | " + Constants.CURRENCY + "%-9.0f | " + Constants.CURRENCY + "%-11.0f | " + Constants.CURRENCY + "%-7.0f | " + Constants.CURRENCY + "%-11.0f | " +
                                ConsoleColors.colorize(Constants.CURRENCY + "%-9.0f", (sc.isPremium() ? ConsoleColors.COLOR_PREMIUM : ConsoleColors.COLOR_STANDARD), ConsoleColors.BOLD) + "\n",
                        mode.getDisplayName(), sc.getDisplayName(),
                        fb.getBaseFare(), resOrAir, fb.getGstAmount(), fb.getConvenienceFee(), fb.getTotalFare());
            }
        }
        System.out.println("=========================================================================================\n");
    }

    // -------------------------------------------------------------------------
    // Phase 3: Cities List grouped by tier
    // -------------------------------------------------------------------------

    private static void printCitiesList() {
        CityRepository repo = CityRepository.getInstance();
        List<City> all = repo.getAllCities();

        System.out.println("================================================================================");
        System.out.println(ConsoleColors.colorize("                   ALL 50 DESTINATIONS GROUPED BY TIER", ConsoleColors.CYAN, ConsoleColors.BOLD));
        System.out.println("================================================================================");
        System.out.printf("Total Cities Loaded: %d (8 Tier 1, 27 Tier 2, 15 Tourist Destinations)\n\n", all.size());

        String[] tiers = {"Tier 1", "Tier 2", "Tourist"};
        for (String tier : tiers) {
            List<City> tierCities = repo.getCitiesByTier(tier);
            System.out.println(ConsoleColors.colorize("--- " + tier.toUpperCase() + " (" + tierCities.size() + " CITIES) -------------------------------------------------", ConsoleColors.YELLOW, ConsoleColors.BOLD));

            for (City c : tierCities) {
                String connectivity = String.format("Airport: %-3s | Station: %-3s",
                        (c.hasAirport() ? "Yes" : "No"), (c.hasStation() ? "Yes" : "No"));
                String places = String.join(", ", c.getTouristPlaces());

                System.out.printf("  [%02d] %-18s (%-16s) | %s\n", c.getId(), c.getName(), c.getState(), connectivity);
                System.out.printf("       Tourist Places: %s\n", places);
            }
            System.out.println();
        }
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
            System.out.println("================================================================================");
            System.out.println(ConsoleColors.colorize("TRANSPORT MODE UNAVAILABLE", ConsoleColors.RED, ConsoleColors.BOLD));
            System.out.println("================================================================================");
            System.out.printf("Route  : %s -> %s\n", source.getName(), destination.getName());
            System.out.printf("Mode   : %s\n", mode.getDisplayName());
            System.out.printf("Reason : %s\n", route.getInoperableReason());
            System.out.println("================================================================================");
            return;
        }

        List<Trip> trips = TripGenerator.generateTrips(route, date);

        System.out.println("================================================================================");
        System.out.println(ConsoleColors.colorize(
                String.format("TRIPS FOUND: %s -> %s (%s)", source.getName().toUpperCase(), destination.getName().toUpperCase(), mode.getDisplayName().toUpperCase()),
                ConsoleColors.CYAN, ConsoleColors.BOLD
        ));
        System.out.printf("Date: %s | Distance: %.1f km | Direct: %.1f km | Trips Available: %d\n",
                date.format(DATE_FORMATTER), route.getDistanceKm(), route.getDirectDistanceKm(), trips.size());
        System.out.println("================================================================================\n");

        for (int i = 0; i < trips.size(); i++) {
            Trip trip = trips.get(i);
            System.out.printf("Trip #%d: %s [%s]\n", (i + 1), trip.getTransport().getOperatorName(), trip.getTripId());
            System.out.printf("  Departure : %s | Arrival: %s | Duration: %s | Distance: %.1f km\n",
                    trip.getDepartureTime().format(Trip.TIME_FORMATTER), trip.getFormattedArrival(),
                    trip.getFormattedDuration(), route.getDistanceKm());

            System.out.print("  Seats Left: ");
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<SeatClass, SeatLayout> entry : trip.getTransport().getSeatLayouts().entrySet()) {
                SeatClass sc = entry.getKey();
                SeatLayout layout = entry.getValue();
                if (sb.length() > 0) sb.append(" | ");
                sb.append(sc.getDisplayName()).append(": ")
                  .append(layout.getAvailableSeatsCount()).append("/").append(layout.getTotalSeats());
            }
            System.out.println(sb.toString());
            System.out.println("--------------------------------------------------------------------------------");
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
