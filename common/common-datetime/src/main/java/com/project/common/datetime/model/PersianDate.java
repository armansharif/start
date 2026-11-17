package com.project.common.datetime.model;

/**
 * A date in the Solar Hijri (Jalali / Persian) calendar.
 *
 * <p>This type is a pure value object: it carries a calendar date and nothing else.
 * Conversion to and from {@link java.time.LocalDate} is the responsibility of
 * {@code PersianDateConverter}, so that the calendar algorithm stays in one place.</p>
 *
 * <p>A {@code PersianDate} is never stored in the database. Persistence always uses
 * {@link java.time.LocalDate}; Persian dates exist only at the API boundary and in
 * presentation.</p>
 *
 * @param year  Persian year (1..3000, the range supported by the underlying algorithm)
 * @param month Persian month (1 = Farvardin .. 12 = Esfand)
 * @param day   day of month (1..31)
 */
public record PersianDate(int year, int month, int day) {

    public static final int MIN_YEAR = 1;
    public static final int MAX_YEAR = 3000;

    public PersianDate {
        if (year < MIN_YEAR || year > MAX_YEAR) {
            throw new IllegalArgumentException("Persian year out of supported range: " + year);
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Persian month out of range: " + month);
        }
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException("Persian day of month out of range: " + day);
        }
    }

    public static PersianDate of(int year, int month, int day) {
        return new PersianDate(year, month, day);
    }

    @Override
    public String toString() {
        return String.format("%04d/%02d/%02d", year, month, day);
    }
}
