package com.project.common.logging.errorlog;

import com.project.common.persistence.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Persisted unexpected failure.
 *
 * <p>Only genuine system failures end up here; business exceptions are part of the
 * contract and are just logged. The stack trace is truncated to a size the column can
 * hold, because an error logger that fails on long traces is worse than useless.</p>
 */
@Entity
@Table(name = "SYSTEM_ERROR_LOG")
public class SystemErrorLog extends BaseEntity {

    public static final int MAX_STACK_TRACE_LENGTH = 8000;
    public static final int MAX_MESSAGE_LENGTH = 2000;

    @Column(name = "ERROR_CODE", nullable = false, length = 50)
    private String errorCode;

    @Column(name = "EXCEPTION_CLASS", nullable = false, length = 300)
    private String exceptionClass;

    @Column(name = "ERROR_MESSAGE", length = MAX_MESSAGE_LENGTH)
    private String errorMessage;

    @Column(name = "STACK_TRACE", length = MAX_STACK_TRACE_LENGTH)
    private String stackTrace;

    @Column(name = "REQUEST_URI", length = 500)
    private String requestUri;

    @Column(name = "HTTP_METHOD", length = 10)
    private String httpMethod;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "CORRELATION_ID", length = 100)
    private String correlationId;

    @Column(name = "REQUEST_TIMESTAMP", nullable = false)
    private Instant requestTimestamp;

    protected SystemErrorLog() {
        // for JPA
    }

    public SystemErrorLog(String errorCode,
                          String exceptionClass,
                          String errorMessage,
                          String stackTrace,
                          String requestUri,
                          String httpMethod,
                          Long userId,
                          String correlationId,
                          Instant requestTimestamp) {
        this.errorCode = errorCode;
        this.exceptionClass = truncate(exceptionClass, 300);
        this.errorMessage = truncate(errorMessage, MAX_MESSAGE_LENGTH);
        this.stackTrace = truncate(stackTrace, MAX_STACK_TRACE_LENGTH);
        this.requestUri = truncate(requestUri, 500);
        this.httpMethod = truncate(httpMethod, 10);
        this.userId = userId;
        this.correlationId = truncate(correlationId, 100);
        this.requestTimestamp = requestTimestamp;
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public Long getUserId() {
        return userId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Instant getRequestTimestamp() {
        return requestTimestamp;
    }
}
