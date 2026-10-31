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
import com.travel.service.TripGenerator;
import com.travel.ui.SeatMapRenderer;
import com.travel.util.ConsoleColors;

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
        if (args.length == 0) {
            runDemo(null);
            return;
        }

        for (int i = 0; i < args.length; i++) {
            String arg = args[i].trim();
            if ("--ascii".equalsIgnoreCase(arg)) {
                ConsoleColors.setAsciiMode(true);
            } else if ("--nocolor".equalsIgnoreCase(arg)) {
                ConsoleColors.setNoColor(true);
            } else if ("--cities".equalsIgnoreCase(arg)) {
                printCitiesList();
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

        // Default fallback
        runDemo(null);
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

        // Check if route is operable
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
                    trip.getDepartureTime(), trip.getArrivalTime(), trip.getFormattedDuration(), route.getDistanceKm());

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
