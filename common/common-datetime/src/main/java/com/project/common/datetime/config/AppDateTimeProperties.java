package com.project.common.datetime.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.ZoneId;
import java.util.Locale;

/**
 * Date/time configuration. Everything is externally configurable so that the same
 * artifact can serve an Iranian deployment and a neutral one without code changes.
 */
@ConfigurationProperties(prefix = "app.datetime")
public class AppDateTimeProperties {

    /** Zone used for presentation and for turning calendar days into instants. */
    private ZoneId zone = ZoneId.of("Asia/Tehran");

    /** Default locale for messages and formatting. */
    private Locale locale = Locale.forLanguageTag("fa-IR");

    /** When true, display oriented Persian fields use Persian digits (۰۱۲...). */
    private boolean persianDigitsOutput = true;

    /** Accepted Persian input formats, documented in docs/datetime-guidelines.md. */
    private String persianOutputPattern = "yyyy/MM/dd";

    public ZoneId getZone() {
        return zone;
    }

    public void setZone(ZoneId zone) {
        this.zone = zone;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public boolean isPersianDigitsOutput() {
        return persianDigitsOutput;
    }

    public void setPersianDigitsOutput(boolean persianDigitsOutput) {
        this.persianDigitsOutput = persianDigitsOutput;
    }

    public String getPersianOutputPattern() {
        return persianOutputPattern;
    }

    public void setPersianOutputPattern(String persianOutputPattern) {
        this.persianOutputPattern = persianOutputPattern;
    }
}
