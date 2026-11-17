package com.project.common.web.error;

import java.time.Instant;

/**
 * Data of an unexpected failure, handed to the recorder for persistence.
 *
 * <p>The record is produced by the web layer but persisted by {@code common-logging};
 * the port keeps both modules independent of each other.</p>
 *
 * @param errorCode       stable code returned to the client
 * @param exceptionClass  fully qualified class name of the failure
 * @param errorMessage    short message (never contains credentials or tokens)
 * @param stackTrace      full stack trace, stored for diagnostics only
 * @param requestUri      URI of the failing request
 * @param httpMethod      HTTP method of the failing request
 * @param userId          acting user, or the system id
 * @param correlationId   request correlation id
 * @param requestTimestamp when the request reached the server
 */
public record SystemErrorRecord(
        String errorCode,
        String exceptionClass,
        String errorMessage,
        String stackTrace,
        String requestUri,
        String httpMethod,
        Long userId,
        String correlationId,
        Instant requestTimestamp) {
}
