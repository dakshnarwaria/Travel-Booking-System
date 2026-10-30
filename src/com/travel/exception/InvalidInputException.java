package com.travel.exception;

/**
 * Demonstrates: Custom Unchecked (Runtime) Exception.
 * Thrown when user input violates parsing rules, range bounds, or formatting requirements.
 */
public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
