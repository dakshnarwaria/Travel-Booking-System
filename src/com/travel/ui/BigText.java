package com.travel.ui;

import com.travel.util.ConsoleColors;

import java.util.ArrayList;
import java.util.List;

/** Renders the welcome heading with a reusable five-row block font. */
public final class BigText {

    private static final String[] COLORS = {
        ConsoleColors.CYAN,
        ConsoleColors.BRIGHT_CYAN,
        ConsoleColors.GREEN,
        ConsoleColors.BRIGHT_YELLOW,
        ConsoleColors.YELLOW,
        ConsoleColors.MAGENTA
    };

    private BigText() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // OOPS: encapsulates the glyph font and its rendering behavior.
    public static List<String> bannerLines() {
        List<String> lines = new ArrayList<>();
        addBlockText(lines, "TRAVEL TICKET");
        lines.add("");
        addBlockText(lines, "BOOKING");
        lines.add(ScreenStyle.center("Bus  |  Train  |  Flight", ScreenStyle.FRAME_WIDTH));
        lines.add(ScreenStyle.divider());
        return lines;
    }

    public static void printBanner() {
        for (String line : bannerLines()) {
            System.out.println(line);
        }
    }

    private static void addBlockText(List<String> lines, String text) {
        for (int row = 0; row < 5; row++) {
            StringBuilder rendered = new StringBuilder();
            int letterIndex = 0;
            for (int index = 0; index < text.length(); index++) {
                char letter = text.charAt(index);
                if (letter == ' ') {
                    rendered.append("    ");
                    continue;
                }
                String glyph = glyph(letter)[row];
                if (ConsoleColors.isAsciiMode()) {
                    glyph = glyph.replace('█', '#');
                }
                rendered.append(ConsoleColors.colorize(
                        glyph, COLORS[letterIndex % COLORS.length]));
                letterIndex++;
                if (index + 1 < text.length() && text.charAt(index + 1) != ' ') {
                    rendered.append(' ');
                }
            }
            lines.add(ScreenStyle.center(rendered.toString(), ScreenStyle.FRAME_WIDTH));
        }
    }

    private static String[] glyph(char letter) {
        switch (letter) {
            case 'A':
                return new String[]{" ███ ", "█   █", "█████", "█   █", "█   █"};
            case 'B':
                return new String[]{"████ ", "█   █", "████ ", "█   █", "████ "};
            case 'C':
                return new String[]{" ████", "█    ", "█    ", "█    ", " ████"};
            case 'E':
                return new String[]{"█████", "█    ", "████ ", "█    ", "█████"};
            case 'G':
                return new String[]{" ████", "█    ", "█  ██", "█   █", " ████"};
            case 'I':
                return new String[]{"█████", "  █  ", "  █  ", "  █  ", "█████"};
            case 'K':
                return new String[]{"█   █", "█  █ ", "███  ", "█  █ ", "█   █"};
            case 'L':
                return new String[]{"█    ", "█    ", "█    ", "█    ", "█████"};
            case 'N':
                return new String[]{"█   █", "██  █", "█ █ █", "█  ██", "█   █"};
            case 'O':
                return new String[]{" ███ ", "█   █", "█   █", "█   █", " ███ "};
            case 'R':
                return new String[]{"████ ", "█   █", "████ ", "█  █ ", "█   █"};
            case 'T':
                return new String[]{"█████", "  █  ", "  █  ", "  █  ", "  █  "};
            case 'V':
                return new String[]{"█   █", "█   █", "█   █", " █ █ ", "  █  "};
            default:
                throw new IllegalArgumentException("Unsupported block letter: " + letter);
        }
    }
}
