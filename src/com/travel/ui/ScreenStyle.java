package com.travel.ui;

import com.travel.util.ConsoleColors;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Provides visible-width alignment and a consistent console frame. */
public final class ScreenStyle {

    public static final int FRAME_WIDTH = 76;
    private static final int CONTENT_WIDTH = FRAME_WIDTH - 2;
    private static final Pattern ANSI_COLOR =
            Pattern.compile("\\u001B\\[[;\\d]*m");

    private ScreenStyle() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static int visibleLength(String text) {
        if (text == null) {
            return 0;
        }
        String visible = ANSI_COLOR.matcher(text).replaceAll("");
        return visible.codePointCount(0, visible.length());
    }

    public static String padRight(String text, int width) {
        String fitted = fitVisible(text, width);
        return fitted + spaces(width - visibleLength(fitted));
    }

    public static String center(String text, int width) {
        String fitted = fitVisible(text, width);
        int padding = width - visibleLength(fitted);
        int left = padding / 2;
        return spaces(left) + fitted + spaces(padding - left);
    }

    public static String topBorder() {
        return edgeTop() + repeat(horizontal(), CONTENT_WIDTH) + edgeTopRight();
    }

    public static String bottomBorder() {
        return edgeBottomLeft() + repeat(horizontal(), CONTENT_WIDTH) + edgeBottomRight();
    }

    public static String frameLine(String text) {
        return vertical() + padRight(text, CONTENT_WIDTH) + vertical();
    }

    public static String ruleContent() {
        return repeat(horizontal(), CONTENT_WIDTH);
    }

    public static String divider() {
        return repeat(horizontal(), FRAME_WIDTH);
    }

    public static List<String> frame(String... lines) {
        List<String> content = new ArrayList<>();
        for (String line : lines) {
            content.add(line);
        }
        return frame(content);
    }

    public static List<String> frame(List<String> lines) {
        List<String> framed = new ArrayList<>();
        framed.add(topBorder());
        for (String line : lines) {
            framed.add(frameLine(line));
        }
        framed.add(bottomBorder());
        return framed;
    }

    private static String fitVisible(String text, int width) {
        String value = text == null ? "" : text;
        StringBuilder fitted = new StringBuilder();
        int visible = 0;
        for (int index = 0; index < value.length();) {
            if (value.charAt(index) == '\u001B') {
                var matcher = ANSI_COLOR.matcher(value);
                matcher.region(index, value.length());
                if (matcher.lookingAt()) {
                    fitted.append(matcher.group());
                    index = matcher.end();
                    continue;
                }
            }
            if (visible == width) {
                if (ANSI_COLOR.matcher(value).find()) {
                    fitted.append(ConsoleColors.RESET);
                }
                break;
            }
            int codePoint = value.codePointAt(index);
            fitted.appendCodePoint(codePoint);
            index += Character.charCount(codePoint);
            visible++;
        }
        return fitted.toString();
    }

    private static String spaces(int count) {
        return repeat(' ', count);
    }

    private static String repeat(char character, int count) {
        StringBuilder result = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            result.append(character);
        }
        return result.toString();
    }

    private static char horizontal() {
        return ConsoleColors.isAsciiMode() ? '-' : '─';
    }

    private static char vertical() {
        return ConsoleColors.isAsciiMode() ? '|' : '│';
    }

    private static char edgeTop() {
        return ConsoleColors.isAsciiMode() ? '+' : '┌';
    }

    private static char edgeTopRight() {
        return ConsoleColors.isAsciiMode() ? '+' : '┐';
    }

    private static char edgeBottomLeft() {
        return ConsoleColors.isAsciiMode() ? '+' : '└';
    }

    private static char edgeBottomRight() {
        return ConsoleColors.isAsciiMode() ? '+' : '┘';
    }
}
