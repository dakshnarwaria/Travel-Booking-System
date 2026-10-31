package com.travel.exception;

/**
 * Demonstrates: A custom unchecked exception for bounded input validation.
 */
public class TooManyInvalidAttemptsException extends InvalidInputException {

    public TooManyInvalidAttemptsException() {
        super("Too many invalid attempts.");
    }
}
