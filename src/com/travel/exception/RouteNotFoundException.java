package com.travel.exception;

/**
 * Demonstrates: Hierarchical Checked Exception.
 * Thrown when no navigable path or transport schedule connects the requested origin and destination.
 */
public class RouteNotFoundException extends TravelException {
    private final String sourceCity;
    private final String destinationCity;

    public RouteNotFoundException(String sourceCity, String destinationCity) {
        super("No route or transport schedule found between " + sourceCity + " and " + destinationCity + ".");
        this.sourceCity = sourceCity;
        this.destinationCity = destinationCity;
    }

    public String getSourceCity() {
        return sourceCity;
    }

    public String getDestinationCity() {
        return destinationCity;
    }
}
