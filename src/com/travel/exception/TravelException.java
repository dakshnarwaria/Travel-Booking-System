package com.travel.exception;

/**
 * Demonstrates: Custom Checked Exception and Exception Hierarchy Root.
 * Base class for all domain-specific checked exceptions in the Travel Booking System.
 */
public class TravelException extends Exception {
    public TravelException(String message) {
        super(message);
    }

    public TravelException(String message, Throwable cause) {
        super(message, cause);
    }
}
