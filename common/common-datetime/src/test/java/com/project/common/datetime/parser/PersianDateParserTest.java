package com.project.common.datetime.parser;

import com.project.common.core.error.BusinessException;
import com.project.common.datetime.converter.Time4JPersianDateConverter;
import com.project.common.datetime.model.PersianDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersianDateParserTest {

    private final PersianDateParser parser = new PersianDateParser(new Time4JPersianDateConverter());

    @ParameterizedTest
    @ValueSource(strings = {
            "1405/08/24",
            "1405-08-24",
            " 1405/08/24 ",
            "1405 / 08 / 24",
            "\u06F1\u06F4\u06F0\u06F5/\u06F0\u06F8/\u06F2\u06F4",   // Persian digits
            "\u0661\u0664\u0660\u0665/\u0660\u0668/\u0662\u0664"    // Arabic-Indic digits
    })
    void acceptsSupportedFormats(String input) {
        assertThat(parser.parse(input)).isEqualTo(PersianDate.of(1405, 8, 24));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1405/8/24",        // month/day must be two digits
            "1405/08/24/01",    // trailing data
            "1405_08_24",       // unknown separator
            "1405/08-24",       // mixed separators
            "1405/00/10",
            "1405/13/01",
            "1405/07/31",       // month 7 has 30 days
            "1404/12/30",       // 1404 is not a leap year
            "not-a-date"
    })
    void rejectsInvalidValues(String input) {
        assertThatThrownBy(() -> parser.parse(input)).isInstanceOf(BusinessException.class);
        assertThat(parser.canParse(input)).isFalse();
    }

    @Test
    void rejectsNullAndBlank() {
        assertThatThrownBy(() -> parser.parse(null)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> parser.parse("   ")).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> parser.parse("")).isInstanceOf(BusinessException.class);
    }

    @Test
    void neverAutoDetectsTheCalendar() {
        // A Gregorian looking value is interpreted as a Persian date, because the contract
        // declares the field (or its *Persian suffix) explicitly. No silent guessing.
        assertThat(parser.parse("2026-11-15")).isEqualTo(PersianDate.of(2026, 11, 15));
    }

    @Test
    void acceptsLeapDayOfLeapYear() {
        assertThat(parser.parse("1403/12/30")).isEqualTo(PersianDate.of(1403, 12, 30));
    }

    @Test
    void reportsFieldNameInError() {
        assertThatThrownBy(() -> parser.parse("1405/13/01", "birthDatePersian"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid Persian date");
    }
}
