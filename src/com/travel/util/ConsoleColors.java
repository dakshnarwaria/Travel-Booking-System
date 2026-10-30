package com.travel.util;

/**
 * Demonstrates: Final Class, Private Constructor (Preventing Instantiation),
 * Static Constants, and Encapsulation.
 * Manages ANSI terminal escape codes and ASCII fallback rendering modes.
 */
public final class ConsoleColors {

    // Prevent direct instantiation
    private ConsoleColors() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // Reset
    public static final String RESET = "\u001B[0m";

    // Text formatting
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";
    public static final String UNDERLINE = "\u001B[4m";

    // Standard foreground colors
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    public static final String GREY = "\u001B[90m";

    // High intensity foreground colors
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_WHITE = "\u001B[97m";

    // Background colors
    public static final String BG_RED = "\u001B[41m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String BG_YELLOW = "\u001B[43m";
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_CYAN = "\u001B[46m";
    public static final String BG_GREY = "\u001B[100m";

    // Domain-specific UI semantic colors
    public static final String COLOR_STANDARD = GREEN;
    public static final String COLOR_PREMIUM = YELLOW;
    public static final String COLOR_BOOKED = GREY;
    public static final String COLOR_SELECTED = CYAN;

    // Global independent flags
    private static volatile boolean asciiMode = false;
    private static volatile boolean noColor = false;

    public static boolean isAsciiMode() {
        return asciiMode;
    }

    public static void setAsciiMode(boolean mode) {
        asciiMode = mode;
    }

    public static boolean isNoColor() {
        return noColor;
    }

    public static void setNoColor(boolean flag) {
        noColor = flag;
    }

    /**
     * Applies an ANSI color code to the text unless noColor mode is active.
     */
    public static String colorize(String text, String ansiColor) {
        if (noColor || text == null || ansiColor == null) {
            return text;
        }
        return ansiColor + text + RESET;
    }

    /**
     * Applies both formatting and color unless noColor mode is active.
     */
    public static String colorize(String text, String ansiColor, String style) {
        if (noColor || text == null) {
            return text;
        }
        return (style != null ? style : "") + (ansiColor != null ? ansiColor : "") + text + RESET;
    }
}
