package com.travel.model;

import com.travel.enums.TransportType;
import com.travel.exception.InvalidInputException;
import com.travel.util.DistanceCalculator;

import java.io.Serializable;
import java.util.Objects;

/**
 * Demonstrates: Composition (has-a City source, City destination), Encapsulation,
 * and Business Rule Validation.
 * Calculates effective travel distance factored by transport modality road/air factors.
 */
public class Route implements Serializable {

    private static final long serialVersionUID = 1L;

    private final City source;
    private final City destination;
    private final TransportType transportType;
    private final double directDistanceKm;
    private final double distanceKm;

    public Route(City source, City destination, TransportType transportType) {
        if (source == null || destination == null) {
            throw new InvalidInputException("Source and Destination cities cannot be null.");
        }
        if (source.equals(destination)) {
            throw new InvalidInputException("Source and Destination cities cannot be the same.");
        }
        if (transportType == null) {
            throw new InvalidInputException("Transport type cannot be null.");
        }

        this.source = source;
        this.destination = destination;
        this.transportType = transportType;

        this.directDistanceKm = DistanceCalculator.haversine(
                source.getLatitude(), source.getLongitude(),
                destination.getLatitude(), destination.getLongitude()
        );
        this.distanceKm = this.directDistanceKm * transportType.getRoadFactor();
    }

    public City getSource() {
        return source;
    }

    public City getDestination() {
        return destination;
    }

    public TransportType getTransportType() {
        return transportType;
    }

    public double getDirectDistanceKm() {
        return directDistanceKm;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    /**
     * Checks if this transport modality is physically operational between both cities.
     */
    public boolean isOperable() {
        switch (transportType) {
            case BUS:
                return true; // All cities have road/bus connectivity
            case TRAIN:
                return source.hasStation() && destination.hasStation();
            case FLIGHT:
                return source.hasAirport() && destination.hasAirport();
            default:
                return false;
        }
    }

    /**
     * Returns a descriptive reason if this mode is not operational.
     */
    public String getInoperableReason() {
        if (isOperable()) return null;
        if (transportType == TransportType.TRAIN) {
            if (!source.hasStation() && !destination.hasStation()) {
                return "Train stations not available in " + source.getName() + " and " + destination.getName();
            } else if (!source.hasStation()) {
                return "Train station not available in " + source.getName();
            } else {
                return "Train station not available in " + destination.getName();
            }
        } else if (transportType == TransportType.FLIGHT) {
            if (!source.hasAirport() && !destination.hasAirport()) {
                return "Airports not available in " + source.getName() + " and " + destination.getName();
            } else if (!source.hasAirport()) {
                return "Airport not available in " + source.getName();
            } else {
                return "Airport not available in " + destination.getName();
            }
        }
        return "Route not operable";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Route)) return false;
        Route route = (Route) o;
        return Objects.equals(source, route.source) &&
                Objects.equals(destination, route.destination) &&
                transportType == route.transportType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination, transportType);
    }

    @Override
    public String toString() {
        return String.format("%s -> %s via %s (%.1f km)",
                source.getName(), destination.getName(), transportType.getDisplayName(), distanceKm);
    }
}
