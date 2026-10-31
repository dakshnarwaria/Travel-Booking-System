package com.travel.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Demonstrates: Dependency substitution with a controllable clock for deterministic tests.
 */
public final class Clock {

    public static final DateTimeFormatter OVERRIDE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private static volatile LocalDateTime fixedNow;

    private Clock() {
        throw new UnsupportedOperationException("Clock cannot be instantiated.");
    }

    public static LocalDateTime now() {
        LocalDateTime fixed = fixedNow;
        return fixed != null ? fixed : LocalDateTime.now();
    }

    public static void setFixedTime(String value) {
        try {
            fixedNow = LocalDateTime.parse(value, OVERRIDE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid --now value. Expected dd-MM-yyyy HH:mm.", e
            );
        }
    }

    /** Clears a test clock override and restores the system clock. */
    public static void clearFixedTime() {
        fixedNow = null;
    }
}
