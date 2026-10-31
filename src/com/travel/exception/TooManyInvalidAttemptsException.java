package com.travel.exception;

/**
 * Demonstrates: A custom unchecked exception for bounded input validation.
 */
public class TooManyInvalidAttemptsException extends InvalidInputException {
    private static final long serialVersionUID = 1L;

    public TooManyInvalidAttemptsException() {
        super("Too many invalid attempts.");
    }
}
