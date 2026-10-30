package com.travel;

import com.travel.enums.SeatClass;
import com.travel.enums.SeatStatus;
import com.travel.layout.BerthBayLayout;
import com.travel.layout.CabinLayout;
import com.travel.layout.ChairLayout;
import com.travel.layout.FlightCabinLayout;
import com.travel.layout.SeatLayout;
import com.travel.layout.SleeperDeckLayout;
import com.travel.seat.Seat;
import com.travel.ui.SeatMapRenderer;
import com.travel.util.ConsoleColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Demonstrates: Program Entry Point, Command-Line Option Dispatching,
 * Deterministic Seeding, and Non-Interactive Demonstration.
 */
public class Main {

    private static final long SEED = 42L;
    private static final double BOOKED_RATIO = 0.35; // 35% deterministic pre-sold seats

    public static void main(String[] args) {
        boolean isDemo = false;
        String filterName = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i].trim();
            if ("--ascii".equalsIgnoreCase(arg)) {
                ConsoleColors.setAsciiMode(true);
            } else if ("--nocolor".equalsIgnoreCase(arg)) {
                ConsoleColors.setNoColor(true);
            } else if ("--demo".equalsIgnoreCase(arg)) {
                isDemo = true;
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    filterName = args[++i].trim();
                }
            }
        }

        // Default to non-interactive demo if executed with no arguments
        if (args.length == 0) {
            isDemo = true;
        }

        if (isDemo) {
            runDemo(filterName);
        } else {
            runDemo(null);
        }
    }

    private static void runDemo(String filterName) {
        List<SeatLayout> layouts = createAllLayouts();

        if (filterName != null && !filterName.isEmpty()) {
            String target = filterName.toLowerCase();
            SeatLayout matched = null;

            // 1. Exact match on full title ("Bus Sleeper", "Bus AC Sleeper")
            for (SeatLayout layout : layouts) {
                if (layout.getSeatClass().toString().equalsIgnoreCase(target)
                        || layout.getSeatClass().getDisplayName().equalsIgnoreCase(target)
                        || layout.getSeatClass().name().equalsIgnoreCase(target)) {
                    matched = layout;
                    break;
                }
            }

            // 2. Fallback to contains
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
            // Render all 10 classes without PREVIEW banners
            for (SeatLayout layout : layouts) {
                SeatMapRenderer.renderLayout(layout);
            }
        }
    }

    /**
     * Instantiates all 10 seat layouts matching PLAN.md Section 4 seat counts.
     */
    public static List<SeatLayout> createAllLayouts() {
        List<SeatLayout> list = new ArrayList<>();
        long seedOffset = 0;

        // 1. Bus Seater (non-AC) - 40 seats
        SeatLayout busSeater = new ChairLayout(SeatClass.BUS_SEATER, 10, 2, 2, new char[]{'A', 'B', 'C', 'D'});
        seedBookedSeats(busSeater, SEED + seedOffset++);
        list.add(busSeater);

        // 2. Bus AC Seater - 40 seats
        SeatLayout busAcSeater = new ChairLayout(SeatClass.BUS_AC_SEATER, 10, 2, 2, new char[]{'A', 'B', 'C', 'D'});
        seedBookedSeats(busAcSeater, SEED + seedOffset++);
        list.add(busAcSeater);

        // 3. Bus Sleeper (non-AC) - 36 seats
        SeatLayout busSleeper = new SleeperDeckLayout(SeatClass.BUS_SLEEPER);
        seedBookedSeats(busSleeper, SEED + seedOffset++);
        list.add(busSleeper);

        // 4. Bus AC Sleeper - 36 seats
        SeatLayout busAcSleeper = new SleeperDeckLayout(SeatClass.BUS_AC_SLEEPER);
        seedBookedSeats(busAcSleeper, SEED + seedOffset++);
        list.add(busAcSleeper);

        // 5. Train Chair Car - 50 seats
        SeatLayout trainChair = new ChairLayout(SeatClass.TRAIN_CHAIR_CAR, 10, 3, 2, new char[]{'A', 'B', 'C', 'D', 'E'});
        seedBookedSeats(trainChair, SEED + seedOffset++);
        list.add(trainChair);

        // 6. Train Non-AC Sleeper - 48 seats
        SeatLayout trainSleeper = new BerthBayLayout(SeatClass.TRAIN_SLEEPER, 8);
        seedBookedSeats(trainSleeper, SEED + seedOffset++);
        list.add(trainSleeper);

        // 7. Train AC 2-Tier - 36 seats
        SeatLayout train2Tier = new BerthBayLayout(SeatClass.TRAIN_AC_2_TIER, 6);
        seedBookedSeats(train2Tier, SEED + seedOffset++);
        list.add(train2Tier);

        // 8. Train AC 1-Tier - 20 seats
        SeatLayout train1Tier = new CabinLayout(SeatClass.TRAIN_AC_1_TIER);
        seedBookedSeats(train1Tier, SEED + seedOffset++);
        list.add(train1Tier);

        // 9. Flight Economy - 72 seats
        SeatLayout flightEcon = new FlightCabinLayout(SeatClass.FLIGHT_ECONOMY);
        seedBookedSeats(flightEcon, SEED + seedOffset++);
        list.add(flightEcon);

        // 10. Flight Business - 16 seats
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
