package com.travel.exception;

/**
 * Demonstrates: A domain-specific unchecked exception for end-of-input handling.
 */
public class InputEndedException extends InvalidInputException {
    private static final long serialVersionUID = 1L;

    public InputEndedException() {
        super("No more console input is available.");
    }
}
