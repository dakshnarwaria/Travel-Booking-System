package com.travel.util;

import com.travel.exception.InputEndedException;
import com.travel.exception.TooManyInvalidAttemptsException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Demonstrates: Encapsulation, Robust Input Validation, and Exception Handling.
 * Wraps System.in Scanner to ensure fail-safe console inputs without crashing.
 */
public final class InputHelper {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final int MAX_INVALID_ATTEMPTS = 5;

    private InputHelper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Reads a validated non-empty string from console.
     */
    public static String readString(String prompt) {
        int attempts = 0;
        while (true) {
            System.out.print(prompt + ": ");
            String input = readLine(SCANNER);
            if (input != null && !input.trim().isEmpty()) {
                return input.trim();
            }
            System.out.println(ConsoleColors.colorize("Input cannot be empty. Please try again.", ConsoleColors.RED));
            checkAttempts(++attempts);
        }
    }

    /**
     * Reads an integer within a bounded [min, max] range.
     */
    public static int readInt(String prompt, int min, int max) {
        int attempts = 0;
        while (true) {
            System.out.print(prompt + " [" + min + " - " + max + "]: ");
            String line = readLine(SCANNER);
            try {
                int value = Integer.parseInt(line.trim());
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println(ConsoleColors.colorize(
                        "Please enter a number between " + min + " and " + max + ".",
                        ConsoleColors.RED
                ));
                checkAttempts(++attempts);
            } catch (NumberFormatException e) {
                System.out.println(ConsoleColors.colorize(
                        "Invalid number format. Please enter a valid integer.",
                        ConsoleColors.RED
                ));
                checkAttempts(++attempts);
            }
        }
    }

    /**
     * Reads and parses a date in the format 'dd-MM-yyyy'.
     * Validates that the travel date is today or in the future.
     */
    public static LocalDate readDate(String prompt) {
        int attempts = 0;
        while (true) {
            System.out.print(prompt + " (" + DATE_FORMAT + "): ");
            String line = readLine(SCANNER);
            if (line == null || line.trim().isEmpty()) {
                System.out.println(ConsoleColors.colorize("Date cannot be empty.", ConsoleColors.RED));
                checkAttempts(++attempts);
                continue;
            }
            try {
                LocalDate date = LocalDate.parse(line.trim(), DATE_FORMATTER);
                LocalDate today = LocalDate.now();
                if (date.isBefore(today)) {
                    System.out.println(ConsoleColors.colorize(
                            "Travel date cannot be in the past. Today is " + today.format(DATE_FORMATTER) + ".",
                            ConsoleColors.RED
                    ));
                    checkAttempts(++attempts);
                    continue;
                }
                return date;
            } catch (DateTimeParseException e) {
                System.out.println(ConsoleColors.colorize(
                        "Invalid date format. Expected pattern: " + DATE_FORMAT + " (e.g., 15-11-2026).",
                        ConsoleColors.RED
                ));
                checkAttempts(++attempts);
            }
        }
    }

    /**
     * Reads a yes/no confirmation response.
     */
    public static boolean confirm(String prompt) {
        int attempts = 0;
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String line = readLine(SCANNER);
            if (line != null) {
                String trimmed = line.trim().toLowerCase();
                if (trimmed.equals("y") || trimmed.equals("yes")) {
                    return true;
                } else if (trimmed.equals("n") || trimmed.equals("no")) {
                    return false;
                }
            }
            System.out.println(ConsoleColors.colorize("Please respond with 'y' or 'n'.", ConsoleColors.RED));
            checkAttempts(++attempts);
        }
    }

    public static String readLine(Scanner scanner, String prompt) {
        if (prompt != null && !prompt.isEmpty()) {
            System.out.print(prompt);
        }
        return readLine(scanner);
    }

    private static String readLine(Scanner scanner) {
        if (scanner == null || !scanner.hasNextLine()) {
            throw new InputEndedException();
        }
        return scanner.nextLine();
    }

    private static void checkAttempts(int attempts) {
        if (attempts >= MAX_INVALID_ATTEMPTS) {
            throw new TooManyInvalidAttemptsException();
        }
    }
}
