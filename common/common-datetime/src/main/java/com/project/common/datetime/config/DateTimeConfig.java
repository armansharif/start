package com.project.common.datetime.config;

import com.project.common.datetime.converter.PersianDateConverter;
import com.project.common.datetime.converter.Time4JPersianDateConverter;
import com.project.common.datetime.formatter.PersianDateFormatter;
import com.project.common.datetime.parser.PersianDateParser;
import com.project.common.datetime.converter.PersianDateSpringConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Wires the date/time building blocks. Modules inject these beans; they never create
 * their own converter, parser or clock.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AppDateTimeProperties.class)
public class DateTimeConfig {

    @Bean
    public PersianDateConverter persianDateConverter() {
        return new Time4JPersianDateConverter();
    }

    @Bean
    public PersianDateParser persianDateParser(PersianDateConverter converter) {
        return new PersianDateParser(converter);
    }

    @Bean
    public PersianDateFormatter persianDateFormatter(PersianDateConverter converter,
                                                     AppDateTimeProperties properties) {
        return new PersianDateFormatter(converter, properties.isPersianDigitsOutput());
    }

    @Bean
    public PersianDateSpringConverter persianDateSpringConverter(PersianDateParser parser) {
        return new PersianDateSpringConverter(parser);
    }

    /**
     * Application-wide clock. Injectable everywhere instead of {@code Instant.now()} so
     * that time dependent behaviour can be tested deterministically.
     */
    @Bean
    public Clock appClock(AppDateTimeProperties properties) {
        return Clock.system(properties.getZone());
    }
}
