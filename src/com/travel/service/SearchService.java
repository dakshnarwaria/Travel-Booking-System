package com.travel.service;

import com.travel.enums.SeatClass;
import com.travel.enums.TransportType;
import com.travel.layout.SeatLayout;
import com.travel.model.City;
import com.travel.model.Route;
import com.travel.model.Trip;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates: Service Layer Architecture, Functional Programming / Lambdas
 * (Comparator sorting), and Query Filtering.
 * Coordinates route queries, mode feasibility, and sorted trip schedules.
 */
public class SearchService {

    /**
     * Data Transfer Object representing available class summary and starting fare.
     */
    public static class ClassFareInfo {
        private final SeatClass seatClass;
        private final double startingFare;
        private final int seatsLeft;
        private final int totalSeats;

        public ClassFareInfo(SeatClass seatClass, double startingFare, int seatsLeft, int totalSeats) {
            this.seatClass = seatClass;
            this.startingFare = startingFare;
            this.seatsLeft = seatsLeft;
            this.totalSeats = totalSeats;
        }

        public SeatClass getSeatClass() { return seatClass; }
        public double getStartingFare() { return startingFare; }
        public int getSeatsLeft() { return seatsLeft; }
        public int getTotalSeats() { return totalSeats; }

        @Override
        public String toString() {
            return String.format("%s: Starting at ₹%.0f (%d/%d seats left)",
                    seatClass.getDisplayName(), startingFare, seatsLeft, totalSeats);
        }
    }

    /**
     * Determines all transport modes physically available between two cities.
     */
    public List<TransportType> getAvailableModes(City source, City destination) {
        List<TransportType> modes = new ArrayList<>();
        if (source == null || destination == null) return modes;

        for (TransportType type : TransportType.values()) {
            Route r = new Route(source, destination, type);
            if (r.isOperable()) {
                modes.add(type);
            }
        }
        return modes;
    }

    /**
     * Retrieves and sorts scheduled trips by departure time using a Comparator lambda.
     */
    public List<Trip> searchTrips(Route route, LocalDate date) {
        List<Trip> trips = TripGenerator.generateTrips(route, date);

        // Demonstrates: Lambda expression for sorting
        trips.sort(Comparator.comparing(Trip::getDepartureTime));

        return trips;
    }

    /**
     * Summarizes the classes, starting fares, and seat vacancies for a given trip.
     */
    public List<ClassFareInfo> getClassesForTrip(Trip trip) {
        List<ClassFareInfo> list = new ArrayList<>();
        if (trip == null) return list;

        double distanceKm = trip.getRoute().getDistanceKm();
        for (Map.Entry<SeatClass, SeatLayout> entry : trip.getTransport().getSeatLayouts().entrySet()) {
            SeatClass sc = entry.getKey();
            SeatLayout layout = entry.getValue();

            PricingService.FareBreakdown fb = PricingService.calculateFare(sc, distanceKm, null);
            list.add(new ClassFareInfo(sc, fb.getTotalFare(), layout.getAvailableSeatsCount(), layout.getTotalSeats()));
        }
        return list;
    }
}
