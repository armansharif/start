package com.project.common.datetime.converter;

import com.project.common.datetime.model.PersianDate;
import net.time4j.PlainDate;
import net.time4j.calendar.PersianCalendar;

import java.time.LocalDate;

/**
 * {@link PersianDateConverter} backed by Time4J, which implements the Borkowski
 * algorithm of the official Iranian calendar (valid far beyond the years this
 * application will ever need).
 *
 * <p>Time4J is referenced only here. No other class in the platform imports it, which
 * keeps the dependency replaceable (see {@code docs/persian-calendar.md}).</p>
 */
public class Time4JPersianDateConverter implements PersianDateConverter {

    @Override
    public LocalDate toGregorian(int year, int month, int day) {
        requireValid(year, month, day);
        // Time4J exposes calendar conversion through CalendarDate#transform(Class).
        return PersianCalendar.of(year, month, day).transform(PlainDate.class).toTemporalAccessor();
    }

    @Override
    public PersianDate toPersian(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Gregorian date must not be null");
        }
        PersianCalendar calendar = PlainDate.from(date).transform(PersianCalendar.class);
        return new PersianDate(
                calendar.getYear(),
                calendar.getMonth().getValue(),
                calendar.getDayOfMonth());
    }

    @Override
    public boolean isLeapYear(int persianYear) {
        if (persianYear < PersianDate.MIN_YEAR || persianYear > PersianDate.MAX_YEAR) {
            throw new IllegalArgumentException("Persian year out of supported range: " + persianYear);
        }
        return PersianCalendar.of(persianYear, 1, 1).isLeapYear();
    }

    @Override
    public int lengthOfMonth(int persianYear, int persianMonth) {
        if (persianYear < PersianDate.MIN_YEAR || persianYear > PersianDate.MAX_YEAR) {
            throw new IllegalArgumentException("Persian year out of supported range: " + persianYear);
        }
        if (persianMonth < 1 || persianMonth > 12) {
            throw new IllegalArgumentException("Persian month out of range: " + persianMonth);
        }
        return PersianCalendar.of(persianYear, persianMonth, 1).lengthOfMonth();
    }

    private static void requireValid(int year, int month, int day) {
        // PersianCalendar.of validates month/day consistency, including Esfand 30 of leap years.
        if (year < PersianDate.MIN_YEAR || year > PersianDate.MAX_YEAR) {
            throw new IllegalArgumentException("Persian year out of supported range: " + year);
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Persian month out of range: " + month);
        }
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException("Persian day of month out of range: " + day);
        }
    }

}
