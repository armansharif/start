package com.project.common.datetime.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateRangeTest {

    private static final ZoneId TEHRAN = ZoneId.of("Asia/Tehran");

    @Test
    void singleDayRangeIsHalfOpenAtTehranMidnight() {
        DateRange range = DateRange.singleDay(LocalDate.of(2026, 11, 15));

        // Tehran is UTC+03:30, so local midnight is 20:30 of the previous UTC day.
        assertThat(range.startInstant(TEHRAN)).isEqualTo(Instant.parse("2026-11-14T20:30:00Z"));
        assertThat(range.endExclusiveInstant(TEHRAN)).isEqualTo(Instant.parse("2026-11-15T20:30:00Z"));
    }

    @Test
    void monthRangeEndsAtStartOfTheFollowingDay() {
        DateRange range = DateRange.of(LocalDate.of(2026, 10, 23), LocalDate.of(2026, 11, 21));

        assertThat(range.startInstant(TEHRAN)).isEqualTo(Instant.parse("2026-10-22T20:30:00Z"));
        assertThat(range.endExclusiveInstant(TEHRAN)).isEqualTo(Instant.parse("2026-11-21T20:30:00Z"));
        assertThat(range.contains(LocalDate.of(2026, 11, 21))).isTrue();
        assertThat(range.contains(LocalDate.of(2026, 11, 22))).isFalse();
    }

    @Test
    void rejectsInvertedRange() {
        assertThatThrownBy(() -> DateRange.of(LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 15)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
