package com.minimarket.util;

public final class SecurityUtils {
    private SecurityUtils() {
        // Prevent instantiation
    }

    /**
     * Sanitizes strings to prevent CRLF injection in logs by replacing newlines and carriage returns with underscores.
     * @param input The string to sanitize.
     * @return The sanitized string.
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("[\r\n]", "_");
    }
}
