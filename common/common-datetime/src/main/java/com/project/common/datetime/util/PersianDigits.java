package com.project.common.datetime.util;

/**
 * Converts Persian/Arabic-Indic digits to ASCII digits and back.
 *
 * <p>Users paste dates from different keyboards, so the parser accepts both digit
 * families. Canonical fields in API responses always use ASCII digits; Persian digits
 * are only used for display, and only when enabled by configuration.</p>
 */
public final class PersianDigits {

    private static final char ZERO_ASCII = '0';
    private static final char PERSIAN_ZERO = '\u06F0';
    private static final char ARABIC_ZERO = '\u0660';

    private PersianDigits() {
    }

    /**
     * @return the input with Persian and Arabic-Indic digits replaced by ASCII digits
     */
    public static String toAscii(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c >= PERSIAN_ZERO && c <= PERSIAN_ZERO + 9) {
                chars[i] = (char) (ZERO_ASCII + (c - PERSIAN_ZERO));
            } else if (c >= ARABIC_ZERO && c <= ARABIC_ZERO + 9) {
                chars[i] = (char) (ZERO_ASCII + (c - ARABIC_ZERO));
            }
        }
        return new String(chars);
    }

    /**
     * @return the input with ASCII digits replaced by Persian digits
     */
    public static String toPersian(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c >= ZERO_ASCII && c <= ZERO_ASCII + 9) {
                chars[i] = (char) (PERSIAN_ZERO + (c - ZERO_ASCII));
            }
        }
        return new String(chars);
    }

    /**
     * Removes every whitespace character, including the ones users type around separators.
     */
    public static String stripWhitespace(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder result = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (!Character.isWhitespace(c) && c != '\u200C' && c != '\u200F' && c != '\u200E') {
                result.append(c);
            }
        }
        return result.toString();
    }
}
