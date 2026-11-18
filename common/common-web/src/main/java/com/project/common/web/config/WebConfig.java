package com.project.common.web.config;

import com.project.common.datetime.converter.PersianDateSpringConverter;
import com.project.common.datetime.parser.PersianDateParser;
import com.project.common.web.error.MessageResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.Locale;

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

    /**
     * Explicit message source.
     *
     * <p>Relies on nothing but {@code spring.messages.basename}: Boot's auto-configuration
     * only activates when it can locate a bundle for the default locale, and when it does
     * not, the context quietly falls back to an empty {@code DelegatingMessageSource}, which
     * makes every error look like the hard coded English default. Declaring the source here
     * makes message loading explicit and UTF-8 from the start.</p>
     */
    @Bean
    public MessageSource messageSource(@Value("${spring.messages.basename:messages/messages}") String basenames) {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames(Arrays.stream(basenames.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .toArray(String[]::new));
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);
        source.setUseCodeAsDefaultMessage(false);
        return source;
    }

    /**
     * Message resolver, pinned to the configured locale when the application fixes one.
     *
     * <p>With {@code spring.web.locale-resolver=fixed} the locale is the application's
     * language, not the caller's: {@code spring.web.locale}. Resolving it here instead of
     * reading {@code LocaleContextHolder} matters because responses produced outside the
     * {@code DispatcherServlet} — Spring Security's 401/403 envelopes, for instance — start
     * with no request locale, which used to fall back to the JVM default and answer in
     * English.</p>
     */
    @Bean
    public MessageResolver messageResolver(MessageSource messageSource,
                                           @Value("${spring.web.locale:}") String configuredLocale,
                                           @Value("${spring.web.locale-resolver:accept-header}") String localeResolver) {
        boolean fixed = !configuredLocale.isBlank() && "fixed".equalsIgnoreCase(localeResolver.trim());
        Locale fixedLocale = fixed ? Locale.forLanguageTag(configuredLocale.trim().replace('_', '-')) : null;
        return new MessageResolver(messageSource, fixedLocale);
    }
}
