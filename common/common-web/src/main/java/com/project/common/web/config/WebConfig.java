package com.project.common.web.config;

import com.project.common.datetime.converter.PersianDateSpringConverter;
import com.project.common.datetime.parser.PersianDateParser;
import com.project.common.web.error.MessageResolver;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web layer wiring: type conversion and message resolution.
 *
 * <p>The Persian date converter is registered globally so that controllers can accept
 * {@code PersianDate} parameters and search models can use Persian date strings without
 * any parsing code of their own.</p>
 */
@Configuration(proxyBeanMethods = false)
public class WebConfig implements WebMvcConfigurer {

    private final PersianDateParser persianDateParser;

    public WebConfig(PersianDateParser persianDateParser) {
        this.persianDateParser = persianDateParser;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new PersianDateSpringConverter(persianDateParser));
    }

    @Bean
    public MessageResolver messageResolver(MessageSource messageSource) {
        return new MessageResolver(messageSource);
    }
}
