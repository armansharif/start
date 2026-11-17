package com.project.common.datetime.converter;

import com.project.common.datetime.model.PersianDate;
import com.project.common.datetime.parser.PersianDateParser;
import org.springframework.core.convert.converter.Converter;

import java.util.Objects;

/**
 * Spring {@link Converter} that lets controllers and configuration bind Persian date
 * strings to {@link PersianDate} (for example query parameters).
 *
 * <p>Conversion and validation happen here once, at the boundary of the system, and
 * never again inside controllers.</p>
 */
public class PersianDateSpringConverter implements Converter<String, PersianDate> {

    private final PersianDateParser parser;

    public PersianDateSpringConverter(PersianDateParser parser) {
        this.parser = Objects.requireNonNull(parser, "parser must not be null");
    }

    @Override
    public PersianDate convert(String source) {
        return parser.parse(source);
    }
}
