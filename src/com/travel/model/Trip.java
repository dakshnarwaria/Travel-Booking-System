package com.travel.model;

import com.travel.enums.SeatClass;
import com.travel.layout.SeatLayout;
import com.travel.transport.Transport;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Demonstrates: Composition (has-a Transport, has-a Route), Encapsulation,
 * and Date/Time API Integration.
 * Models an scheduled journey connecting origin and destination on a specific calendar date.
 */
public class Trip implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final String tripId;
    private final Transport transport;
    private final Route route;
    private final LocalDate date;
    private final LocalTime departureTime;
    private final LocalTime arrivalTime;
    private final int durationMinutes;

    public Trip(String tripId, Transport transport, Route route, LocalDate date,
                LocalTime departureTime, LocalTime arrivalTime, int durationMinutes) {
        this.tripId = tripId;
        this.transport = transport;
        this.route = route;
        this.date = date;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.durationMinutes = durationMinutes;
    }

    public String getTripId() {
        return tripId;
    }

    public Transport getTransport() {
        return transport;
    }

    public Route getRoute() {
        return route;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public String getFormattedDuration() {
        int hours = durationMinutes / 60;
        int mins = durationMinutes % 60;
        return String.format("%dh %02dm", hours, mins);
    }

    public int getAvailableSeats(SeatClass seatClass) {
        SeatLayout layout = transport.getSeatLayout(seatClass);
        return (layout != null) ? layout.getAvailableSeatsCount() : 0;
    }

    public int getTotalAvailableSeats() {
        int total = 0;
        for (SeatLayout layout : transport.getSeatLayouts().values()) {
            total += layout.getAvailableSeatsCount();
        }
        return total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trip)) return false;
        Trip trip = (Trip) o;
        return Objects.equals(tripId, trip.tripId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tripId);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s -> %s | Dep: %s Arr: %s (%s) | %s",
                tripId, transport.getOperatorName(),
                route.getSource().getName(), route.getDestination().getName(),
                departureTime.format(TIME_FORMATTER), arrivalTime.format(TIME_FORMATTER),
                getFormattedDuration(), date.format(DATE_FORMATTER));
    }
}
