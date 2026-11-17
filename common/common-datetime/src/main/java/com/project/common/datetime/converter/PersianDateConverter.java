package com.project.common.datetime.converter;

import com.project.common.datetime.model.PersianDate;

import java.time.LocalDate;

/**
 * Converts between the Persian (Solar Hijri) and Gregorian calendars.
 *
 * <p>Everything calendar related in the platform goes through this port, so the
 * algorithm library can be replaced without touching any other module. Implementations
 * must be strict: an invalid date is rejected, never silently corrected.</p>
 */
public interface PersianDateConverter {

    /**
     * @throws IllegalArgumentException when the given triple is not a real Persian date
     */
    LocalDate toGregorian(int year, int month, int day);

    default LocalDate toGregorian(PersianDate persianDate) {
        return toGregorian(persianDate.year(), persianDate.month(), persianDate.day());
    }

    PersianDate toPersian(LocalDate date);

    boolean isLeapYear(int persianYear);

    /**
     * @return number of days of the given Persian month (29, 30 or 31)
     */
    int lengthOfMonth(int persianYear, int persianMonth);

    /**
     * @return {@code true} when the triple is a real date (month/day consistent with the year)
     */
    default boolean isValid(int year, int month, int day) {
        if (year < PersianDate.MIN_YEAR || year > PersianDate.MAX_YEAR) {
            return false;
        }
        if (month < 1 || month > 12) {
            return false;
        }
        return day >= 1 && day <= lengthOfMonth(year, month);
    }
}
