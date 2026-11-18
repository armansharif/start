package com.project.common.datetime.formatter;

import com.project.common.datetime.converter.PersianDateConverter;
import com.project.common.datetime.model.PersianDate;
import com.project.common.datetime.util.PersianDigits;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Formats Persian dates for display.
 *
 * <p>Two canonical patterns exist: {@code yyyy/MM/dd} (default) and {@code yyyy-MM-dd}.
 * Latin digits are the safe default for copy/paste and change only when Persian digits
 * are explicitly requested (per application configuration).</p>
 */
public class PersianDateFormatter {

    /** Public pattern of the default format, as documented in the API guidelines. */
    public static final String PATTERN_SLASH = "yyyy/MM/dd";
    /** Public pattern of the alternative format accepted on input. */
    public static final String PATTERN_DASH = "yyyy-MM-dd";

    private static final String FORMAT_SLASH = "%04d/%02d/%02d";
    private static final String FORMAT_DASH = "%04d-%02d-%02d";

    private final PersianDateConverter converter;
    private final boolean persianDigitsByDefault;
    private final ZoneId zone;

    public PersianDateFormatter(PersianDateConverter converter, boolean persianDigitsByDefault) {
        this(converter, persianDigitsByDefault, ZoneId.of("Asia/Tehran"));
    }

    public PersianDateFormatter(PersianDateConverter converter, boolean persianDigitsByDefault, ZoneId zone) {
        this.converter = Objects.requireNonNull(converter, "converter must not be null");
        this.persianDigitsByDefault = persianDigitsByDefault;
        this.zone = Objects.requireNonNull(zone, "zone must not be null");
    }

    public String format(PersianDate date) {
        return format(date, persianDigitsByDefault);
    }

    public String format(PersianDate date, boolean persianDigits) {
        Objects.requireNonNull(date, "date must not be null");
        return applyDigits(String.format(FORMAT_SLASH, date.year(), date.month(), date.day()), persianDigits);
    }

    public String formatDash(PersianDate date) {
        return formatDash(date, persianDigitsByDefault);
    }

    public String formatDash(PersianDate date, boolean persianDigits) {
        Objects.requireNonNull(date, "date must not be null");
        return applyDigits(String.format(FORMAT_DASH, date.year(), date.month(), date.day()), persianDigits);
    }

    /**
     * Converts the stored (Gregorian) value and formats it in the Persian calendar.
     * This is what response DTOs use for their {@code ...Persian} fields.
     */
    public String format(LocalDate date) {
        return format(converter.toPersian(date));
    }

    public String format(LocalDate date, boolean persianDigits) {
        return format(converter.toPersian(date), persianDigits);
    }

    /**
     * Formats a stored instant as a Persian calendar day in the configured zone.
     *
     * <p>An instant is a moment in time; its calendar day depends on the zone, so the zone
     * is part of the formatter instead of being guessed at call sites.</p>
     */
    public String format(Instant instant) {
        Objects.requireNonNull(instant, "instant must not be null");
        return format(instant.atZone(zone).toLocalDate(), persianDigitsByDefault);
    }

    public String format(Instant instant, boolean persianDigits) {
        Objects.requireNonNull(instant, "instant must not be null");
        return format(instant.atZone(zone).toLocalDate(), persianDigits);
    }

    public ZoneId zone() {
        return zone;
    }

    private static String applyDigits(String value, boolean persianDigits) {
        return persianDigits ? PersianDigits.toPersian(value) : value;
    }
}
