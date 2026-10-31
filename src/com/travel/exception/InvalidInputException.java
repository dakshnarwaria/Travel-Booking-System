package com.travel.exception;

/**
 * Demonstrates: Custom Unchecked (Runtime) Exception.
 * Thrown when user input violates parsing rules, range bounds, or formatting requirements.
 */
public class InvalidInputException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
