package com.project.common.web.error;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * Access to the correlation id of the current request.
 *
 * <p>The value is stored in the SLF4J {@link MDC} by the correlation filter, so it is
 * automatically part of every log line. If no value is present (background jobs, tests)
 * a new identifier is generated and stored, never {@code null}.</p>
 */
public final class CorrelationIds {

    /** MDC key shared by logging, error logging and audit. */
    public static final String MDC_KEY = "correlationId";

    /** Request header clients may send to propagate their own identifier. */
    public static final String HEADER = "X-Request-Id";

    private CorrelationIds() {
    }

    public static String current() {
        return MDC.get(MDC_KEY);
    }

    public static String currentOrCreate() {
        String existing = current();
        if (existing != null && !existing.isBlank()) {
            return existing;
        }
        String generated = UUID.randomUUID().toString();
        MDC.put(MDC_KEY, generated);
        return generated;
    }

    public static void set(String correlationId) {
        MDC.put(MDC_KEY, correlationId);
    }

    public static void clear() {
        MDC.remove(MDC_KEY);
    }
}
