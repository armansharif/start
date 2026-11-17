package com.project.common.datetime.converter;

import com.project.common.datetime.model.PersianDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Validates the converter against reference dates (not only with round trips): a
 * round trip can be consistently wrong.
 */
class Time4JPersianDateConverterTest {

    private final PersianDateConverter converter = new Time4JPersianDateConverter();

    @Nested
    @DisplayName("Persian -> Gregorian reference dates")
    class PersianToGregorian {

        @ParameterizedTest(name = "{0}/{1}/{2} = {3}")
        @CsvSource({
                // Persian new year transitions
                "1403, 1, 1, 2024-03-20",
                "1404, 1, 1, 2025-03-21",
                "1405, 1, 1, 2026-03-21",
                "1400, 1, 1, 2021-03-21",
                // leap year: 1403 has 366 days, so Esfand 30 exists
                "1403, 12, 30, 2025-03-20",
                // ordinary end of year
                "1400, 12, 29, 2022-03-20",
                // historical reference date: 22 Bahman 1357
                "1357, 11, 22, 1979-02-11",
                // sample from the API contract documentation
                "1405, 8, 24, 2026-11-15",
                "1405, 8, 25, 2026-11-16"
        })
        void convertsToGregorian(int year, int month, int day, LocalDate expected) {
            assertThat(converter.toGregorian(year, month, day)).isEqualTo(expected);
        }

        @Test
        void rejectsEsfand30InNonLeapYear() {
            assertThatThrownBy(() -> converter.toGregorian(1404, 12, 30))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejectsInvalidMonthAndDay() {
            assertThatThrownBy(() -> converter.toGregorian(1405, 13, 1))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> converter.toGregorian(1405, 7, 31))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Gregorian -> Persian")
    class GregorianToPersian {

        @Test
        void convertsKnownGregorianDates() {
            assertThat(converter.toPersian(LocalDate.of(2026, 11, 15))).isEqualTo(PersianDate.of(1405, 8, 24));
            assertThat(converter.toPersian(LocalDate.of(2025, 3, 20))).isEqualTo(PersianDate.of(1403, 12, 30));
            assertThat(converter.toPersian(LocalDate.of(1979, 2, 11))).isEqualTo(PersianDate.of(1357, 11, 22));
        }

        @Test
        void roundTripsOverALongPeriod() {
            LocalDate date = LocalDate.of(2019, 1, 1);
            LocalDate end = LocalDate.of(2035, 12, 31);
            while (!date.isAfter(end)) {
                PersianDate persian = converter.toPersian(date);
                assertThat(converter.toGregorian(persian))
                        .as("round trip for %s -> %s", date, persian)
                        .isEqualTo(date);
                date = date.plusDays(37);
            }
        }
    }

    @Nested
    @DisplayName("Leap year and month length")
    class CalendarRules {

        @Test
        void detectsLeapYears() {
            assertThat(converter.isLeapYear(1403)).isTrue();
            assertThat(converter.isLeapYear(1404)).isFalse();
            assertThat(converter.isLeapYear(1399)).isTrue();
            assertThat(converter.isLeapYear(1408)).isTrue();
        }

        @Test
        void reportsMonthLengths() {
            assertThat(converter.lengthOfMonth(1405, 1)).isEqualTo(31);
            assertThat(converter.lengthOfMonth(1405, 6)).isEqualTo(31);
            assertThat(converter.lengthOfMonth(1405, 7)).isEqualTo(30);
            assertThat(converter.lengthOfMonth(1405, 11)).isEqualTo(30);
            assertThat(converter.lengthOfMonth(1403, 12)).isEqualTo(30);
            assertThat(converter.lengthOfMonth(1404, 12)).isEqualTo(29);
        }

        @Test
        void validatesTriples() {
            assertThat(converter.isValid(1403, 12, 30)).isTrue();
            assertThat(converter.isValid(1404, 12, 30)).isFalse();
            assertThat(converter.isValid(1405, 0, 1)).isFalse();
        }
    }
}
