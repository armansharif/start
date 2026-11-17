package com.project.common.datetime.parser;

import com.project.common.datetime.converter.PersianDateConverter;
import com.project.common.datetime.error.DateTimeErrorCodes;
import com.project.common.datetime.model.PersianDate;
import com.project.common.datetime.util.PersianDigits;
import com.project.common.core.error.BusinessException;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Strict parser for Persian dates.
 *
 * <p>Accepted input (whitespace and Persian/Arabic digits are normalized first):</p>
 * <ul>
 *   <li>{@code 1405/08/24}</li>
 *   <li>{@code 1405-08-24}</li>
 * </ul>
 *
 * <p>Both separators must be identical; month and day must be written with two digits.
 * Anything else is rejected with a business error. The parser never guesses: it does not
 * auto-correct an invalid day, swap month and day, or accept shortened forms.</p>
 */
public class PersianDateParser {

    private static final Pattern DATE_PATTERN =
            Pattern.compile("^(\\d{4})([/-])(\\d{2})\\2(\\d{2})$");

    private final PersianDateConverter converter;

    public PersianDateParser(PersianDateConverter converter) {
        this.converter = Objects.requireNonNull(converter, "converter must not be null");
    }

    /**
     * @throws BusinessException with {@code COMMON-1602} when the value is not a valid Persian date
     */
    public PersianDate parse(String rawValue) {
        return parse(rawValue, null);
    }

    /**
     * @param fieldName name reported in the error, typically the DTO property
     */
    public PersianDate parse(String rawValue, String fieldName) {
        if (rawValue == null || rawValue.isBlank()) {
            throw invalid(rawValue, fieldName);
        }
        String normalized = PersianDigits.stripWhitespace(PersianDigits.toAscii(rawValue));
        Matcher matcher = DATE_PATTERN.matcher(normalized);
        if (!matcher.matches()) {
            throw invalid(rawValue, fieldName);
        }
        int year = Integer.parseInt(matcher.group(1));
        int month = Integer.parseInt(matcher.group(3));
        int day = Integer.parseInt(matcher.group(4));
        if (!converter.isValid(year, month, day)) {
            throw invalid(rawValue, fieldName);
        }
        return new PersianDate(year, month, day);
    }

    /**
     * @return {@code true} when {@link #parse(String)} would succeed
     */
    public boolean canParse(String rawValue) {
        try {
            parse(rawValue);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private static BusinessException invalid(String rawValue, String fieldName) {
        return new BusinessException(DateTimeErrorCodes.INVALID_PERSIAN_DATE, fieldName, rawValue);
    }
}
