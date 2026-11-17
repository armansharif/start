package com.project.common.datetime.formatter;

import com.project.common.datetime.converter.Time4JPersianDateConverter;
import com.project.common.datetime.model.PersianDate;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PersianDateFormatterTest {

    private final PersianDateFormatter latinFormatter =
            new PersianDateFormatter(new Time4JPersianDateConverter(), false);
    private final PersianDateFormatter persianDigitFormatter =
            new PersianDateFormatter(new Time4JPersianDateConverter(), true);

    @Test
    void formatsWithSlashPattern() {
        assertThat(latinFormatter.format(PersianDate.of(1405, 8, 24))).isEqualTo("1405/08/24");
        assertThat(latinFormatter.formatDash(PersianDate.of(1405, 8, 24))).isEqualTo("1405-08-24");
    }

    @Test
    void padsSingleDigitMonthAndDay() {
        assertThat(latinFormatter.format(PersianDate.of(1405, 1, 5))).isEqualTo("1405/01/05");
    }

    @Test
    void usesPersianDigitsWhenEnabled() {
        assertThat(persianDigitFormatter.format(PersianDate.of(1405, 8, 24)))
                .isEqualTo("\u06F1\u06F4\u06F0\u06F5/\u06F0\u06F8/\u06F2\u06F4");
    }

    @Test
    void convertsStoredGregorianDateForDisplay() {
        assertThat(latinFormatter.format(LocalDate.of(2026, 11, 15))).isEqualTo("1405/08/24");
    }
}
