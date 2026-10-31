package com.travel.service;

import com.travel.enums.SeatClass;
import com.travel.enums.SeatStatus;
import com.travel.enums.TransportType;
import com.travel.layout.SeatLayout;
import com.travel.model.Route;
import com.travel.model.Trip;
import com.travel.seat.ChairSeat;
import com.travel.seat.Seat;
import com.travel.transport.Bus;
import com.travel.transport.Flight;
import com.travel.transport.Train;
import com.travel.transport.Transport;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Demonstrates: Factory Method Pattern, Algorithmic Generation, Deterministic Pseudo-Randomness,
 * and Domain Modeling.
 * Generates daily scheduled trips with realistic travel durations and seeded seat bookings.
 */
public final class TripGenerator {

    private static final double PRE_SOLD_RATIO = 0.35; // 35% pre-sold seats

    // Fictional operator names adhering to Rule 4 & 4b
    private static final String[] BUS_OPERATORS = {
            "Sahyadri Express Lines", "Nilgiri Fleetways", "Garuda Royal Coach", "Mayura Highways"
    };

    private static final String[] TRAIN_OPERATORS = {
            "12105 Deccan Link Superfast", "16381 Malabar Coast Express", "12617 Konkan Star Express", "12903 Western Horizon Express"
    };

    private static final String[] FLIGHT_OPERATORS = {
            "AeroIndia Express (AI-301)", "SkyLink Airways (SL-404)", "CloudAir Shuttle (CA-202)", "BlueSky Aviation (BA-505)"
    };

    private static final LocalTime[] DEPARTURE_TIMES = {
            LocalTime.of(6, 30),
            LocalTime.of(11, 15),
            LocalTime.of(17, 45),
            LocalTime.of(22, 0)
    };

    private TripGenerator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Generates 3 to 4 scheduled trips for a given route and date.
     */
    public static List<Trip> generateTrips(Route route, LocalDate date) {
        if (route == null || date == null || !route.isOperable()) {
            return Collections.emptyList();
        }

        TransportType mode = route.getTransportType();
        double distanceKm = route.getDistanceKm();

        // Calculate travel duration in minutes
        int durationMinutes;
        if (mode == TransportType.BUS) {
            durationMinutes = (int) Math.round((distanceKm / 55.0) * 60.0);
        } else if (mode == TransportType.TRAIN) {
            durationMinutes = (int) Math.round((distanceKm / 65.0) * 60.0);
        } else {
            // Flight: 700 km/h + 30 minutes buffer
            durationMinutes = (int) Math.round((distanceKm / 700.0) * 60.0) + 30;
        }

        // Deterministic seed ensuring consistent results for same search
        long seed = Objects.hash(
                route.getSource().getName().toLowerCase(),
                route.getDestination().getName().toLowerCase(),
                mode.name(),
                date.toString()
        );
        Random rng = new Random(seed);

        int tripCount = 3 + (Math.abs(rng.nextInt()) % 2); // 3 or 4 trips
        List<Trip> trips = new ArrayList<>();

        for (int i = 0; i < tripCount; i++) {
            String opName = getOperatorName(mode, i);
            Transport transport = createTransport(mode, opName, distanceKm);

            // Update prices across layouts for this distance
            for (Map.Entry<SeatClass, SeatLayout> entry : transport.getSeatLayouts().entrySet()) {
                SeatClass sc = entry.getKey();
                SeatLayout layout = entry.getValue();
                double baseFare = PricingService.computeBaseFare(sc, distanceKm);
                layout.updateAllSeatPrices(baseFare);

                // Seed pre-sold seats (~35%)
                for (Seat seat : layout.getAllSeats()) {
                    if (rng.nextDouble() < PRE_SOLD_RATIO) {
                        seat.setStatus(SeatStatus.BOOKED);
                    }
                }
            }

            LocalTime dep = DEPARTURE_TIMES[i % DEPARTURE_TIMES.length].plusMinutes(rng.nextInt(20));
            LocalTime arr = dep.plusMinutes(durationMinutes);
            String tripId = String.format("%s-%03d", mode.name().substring(0, 1), (101 + i));

            trips.add(new Trip(tripId, transport, route, date, dep, arr, durationMinutes));
        }

        return trips;
    }

    private static String getOperatorName(TransportType mode, int index) {
        switch (mode) {
            case BUS:
                return BUS_OPERATORS[index % BUS_OPERATORS.length];
            case TRAIN:
                return TRAIN_OPERATORS[index % TRAIN_OPERATORS.length];
            case FLIGHT:
                return FLIGHT_OPERATORS[index % FLIGHT_OPERATORS.length];
            default:
                return "Express Line " + (index + 1);
        }
    }

    private static Transport createTransport(TransportType mode, String operatorName, double distanceKm) {
        switch (mode) {
            case BUS:
                return new Bus(operatorName);
            case TRAIN:
                return new Train(operatorName, distanceKm);
            case FLIGHT:
                return new Flight(operatorName);
            default:
                throw new IllegalArgumentException("Unknown transport mode: " + mode);
        }
    }
}
