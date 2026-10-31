package com.travel.util;

/**
 * Demonstrates: Static Constants, Final Class, Single Responsibility (no magic strings).
 * Central repository for all display-facing string and numeric constants.
 */
public final class Constants {

    /** Unicode Indian Rupee sign used for all monetary output. */
    public static final String CURRENCY = "\u20B9";

    private Constants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }
}
