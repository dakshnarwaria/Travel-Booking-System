package com.travel;

import com.travel.enums.BookingStatus;
import com.travel.enums.PaymentMode;
import com.travel.enums.SeatClass;
import com.travel.enums.SeatStatus;
import com.travel.enums.SeatType;
import com.travel.enums.TransportType;
import com.travel.util.ConsoleColors;
import com.travel.util.DistanceCalculator;
import com.travel.util.IdGenerator;

/**
 * Demonstrates: Program Entry Point, Static Methods, and System Integration.
 * Serves as the console launcher for the Travel Ticket Booking System.
 * In Phase 1, it demonstrates ANSI color rendering, seat map legends, and core utilities.
 */
public class Main {

    public static void main(String[] args) {
        printBanner();
        printColorLegend();
        printSystemSummary();
    }

    private static void printBanner() {
        System.out.println("================================================================================");
        System.out.println(ConsoleColors.colorize(
                "             TRAVEL TICKET BOOKING SYSTEM - OOPS ARCHITECTURE",
                ConsoleColors.CYAN,
                ConsoleColors.BOLD
        ));
        System.out.println("================================================================================");
        System.out.println("Phase 1 Verification: Foundation, Enums, Interfaces, Exceptions & Color System");
        System.out.println("--------------------------------------------------------------------------------\n");
    }

    /**
     * Prints the visual seat map colour legend according to PLAN.md specifications:
     * - Standard available (Green)
     * - Premium available (Yellow)
     * - Booked (Grey with X)
     * - Selected (Cyan)
     */
    private static void printColorLegend() {
        System.out.println(ConsoleColors.colorize("SEAT MAP COLOUR LEGEND", ConsoleColors.WHITE, ConsoleColors.BOLD));
        System.out.println("----------------------");

        // Standard available seat sample
        String standardSample = ConsoleColors.colorize("  [ 1A ]   Available (Standard) - Green", ConsoleColors.COLOR_STANDARD);
        System.out.println(standardSample);

        // Premium available seat sample
        String premiumSample = ConsoleColors.colorize("  [ 1B ]   Available (Premium / AC / Business) - Yellow", ConsoleColors.COLOR_PREMIUM);
        System.out.println(premiumSample);

        // Booked seat sample with X
        String bookedSample = ConsoleColors.colorize("  [X1C ]   Booked (Sold Out) - Grey with 'X'", ConsoleColors.COLOR_BOOKED);
        System.out.println(bookedSample);

        // Selected seat sample
        String selectedSample = ConsoleColors.colorize("  [ 1D ]   Selected (In Progress) - Cyan", ConsoleColors.COLOR_SELECTED);
        System.out.println(selectedSample);

        System.out.println();

        // Sample Seat Mockup Render
        System.out.println(ConsoleColors.colorize("MOCKUP SEAT BLOCKS PREVIEW:", ConsoleColors.WHITE, ConsoleColors.BOLD));
        System.out.print("  ");
        System.out.print(ConsoleColors.colorize("┌───┐", ConsoleColors.COLOR_STANDARD) + " ");
        System.out.print(ConsoleColors.colorize("┌───┐", ConsoleColors.COLOR_PREMIUM) + " ");
        System.out.print(ConsoleColors.colorize("┌───┐", ConsoleColors.COLOR_BOOKED) + " ");
        System.out.print(ConsoleColors.colorize("┌───┐", ConsoleColors.COLOR_SELECTED));
        System.out.println();

        System.out.print("  ");
        System.out.print(ConsoleColors.colorize("│1A │", ConsoleColors.COLOR_STANDARD) + " ");
        System.out.print(ConsoleColors.colorize("│1B │", ConsoleColors.COLOR_PREMIUM) + " ");
        System.out.print(ConsoleColors.colorize("│X1C│", ConsoleColors.COLOR_BOOKED) + " ");
        System.out.print(ConsoleColors.colorize("│1D │", ConsoleColors.COLOR_SELECTED));
        System.out.println();

        System.out.print("  ");
        System.out.print(ConsoleColors.colorize("└───┘", ConsoleColors.COLOR_STANDARD) + " ");
        System.out.print(ConsoleColors.colorize("└───┘", ConsoleColors.COLOR_PREMIUM) + " ");
        System.out.print(ConsoleColors.colorize("└───┘", ConsoleColors.COLOR_BOOKED) + " ");
        System.out.print(ConsoleColors.colorize("└───┘", ConsoleColors.COLOR_SELECTED));
        System.out.println();

        System.out.print("  ");
        System.out.print(ConsoleColors.colorize("₹899 ", ConsoleColors.COLOR_STANDARD) + " ");
        System.out.print(ConsoleColors.colorize("₹1399", ConsoleColors.COLOR_PREMIUM) + " ");
        System.out.print(ConsoleColors.colorize("Sold ", ConsoleColors.COLOR_BOOKED) + " ");
        System.out.print(ConsoleColors.colorize("Sel  ", ConsoleColors.COLOR_SELECTED));
        System.out.println("\n");
    }

    private static void printSystemSummary() {
        System.out.println(ConsoleColors.colorize("SYSTEM FOUNDATION HEALTH CHECK:", ConsoleColors.WHITE, ConsoleColors.BOLD));
        System.out.println("-------------------------------");
        System.out.println("  * Transport Modes Defined : " + TransportType.values().length + " (Bus, Train, Flight)");
        System.out.println("  * Seat Classes Loaded     : " + SeatClass.values().length + " classes across 3 modes");
        System.out.println("  * Seat Types Active       : " + SeatType.values().length + " (Chair, Sleeper, Berth)");
        System.out.println("  * Seat Statuses Mapped    : " + SeatStatus.values().length + " states");
        System.out.println("  * Payment Modes Configured: " + PaymentMode.values().length + " payment instruments");
        System.out.println("  * Booking Lifecycle States: " + BookingStatus.values().length + " states");

        // Distance test between New Delhi and Mumbai coordinates
        double delhiLat = 28.6139, delhiLon = 77.2090;
        double mumbaiLat = 19.0760, mumbaiLon = 72.8777;
        double directDist = DistanceCalculator.haversine(delhiLat, delhiLon, mumbaiLat, mumbaiLon);
        double busDist = DistanceCalculator.calculateRouteDistance(delhiLat, delhiLon, mumbaiLat, mumbaiLon, TransportType.BUS.getRoadFactor());

        System.out.println(String.format("  * Distance Test (Delhi -> Mumbai) : Direct: %.1f km | Bus Route: %.1f km", directDist, busDist));
        System.out.println("  * Sample PNR Generation           : " + IdGenerator.generatePnr() + ", " + IdGenerator.generatePnr());
        System.out.println("\n" + ConsoleColors.colorize("Status: Phase 1 Foundation successfully compiled and validated.", ConsoleColors.GREEN, ConsoleColors.BOLD));
    }
}
