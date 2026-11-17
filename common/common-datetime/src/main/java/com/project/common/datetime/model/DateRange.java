package com.project.common.datetime.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A closed day range on the calendar, exposed as a half-open instant range.
 *
 * <p>Half-open ranges ({@code >= start && < endExclusive}) avoid the classic
 * end-of-day bugs: fractional seconds, database precision truncation and the
 * difference between {@code DATE} and {@code DATETIME} columns. The end boundary is
 * the start of the day <em>after</em> the last selected day.</p>
 */
public final class DateRange {

    private final LocalDate fromInclusive;
    private final LocalDate toInclusive;

    private DateRange(LocalDate fromInclusive, LocalDate toInclusive) {
        this.fromInclusive = Objects.requireNonNull(fromInclusive, "fromInclusive must not be null");
        this.toInclusive = Objects.requireNonNull(toInclusive, "toInclusive must not be null");
        if (toInclusive.isBefore(fromInclusive)) {
            throw new IllegalArgumentException(
                    "Range end " + toInclusive + " is before range start " + fromInclusive);
        }
    }

    public static DateRange of(LocalDate fromInclusive, LocalDate toInclusive) {
        return new DateRange(fromInclusive, toInclusive);
    }

    public static DateRange singleDay(LocalDate day) {
        return new DateRange(day, day);
    }

    public LocalDate fromInclusive() {
        return fromInclusive;
    }

    public LocalDate toInclusive() {
        return toInclusive;
    }

    /**
     * @return start of the first day in the given zone
     */
    public Instant startInstant(ZoneId zone) {
        return fromInclusive.atStartOfDay(Objects.requireNonNull(zone, "zone must not be null")).toInstant();
    }

    /**
     * @return exclusive upper bound: start of the day after the last selected day
     */
    public Instant endExclusiveInstant(ZoneId zone) {
        return toInclusive.plusDays(1).atStartOfDay(zone).toInstant();
    }

    public ZonedDateTime startZoned(ZoneId zone) {
        return fromInclusive.atStartOfDay(zone);
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(fromInclusive) && !date.isAfter(toInclusive);
    }
}
