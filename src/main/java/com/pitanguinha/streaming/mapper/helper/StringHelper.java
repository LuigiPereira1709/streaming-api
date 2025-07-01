package com.pitanguinha.streaming.mapper.helper;

/**
 * Helper class for string operations.
 * 
 * @since 1.0
 */
public class StringHelper {
    /**
     * Normalizes a string by replacing all occurrences of the specified regex with
     * the given replacement.
     * 
     * @param str         The input string to normalize.
     * @param regex       The regex pattern to match.
     * @param replacement The replacement string.
     * 
     * @return The normalized string.
     */
    public static String normalize(String str, String regex, String replacement) {
        return str == null || str.isEmpty()
                ? str
                : str.replaceAll(regex, replacement);
    }

    /**
     * Capitalizes the first letter of a string and converts the rest to lowercase.
     * 
     * @param str The input string to capitalize.
     * 
     * @return The capitalized string.
     */
    public static String capitalize(String str) {
        return str == null || str.isEmpty()
                ? str
                : str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
