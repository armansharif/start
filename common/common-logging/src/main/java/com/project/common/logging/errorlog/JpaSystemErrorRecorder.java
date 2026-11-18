package com.project.common.logging.errorlog;

import com.project.common.web.error.SystemErrorRecord;
import com.project.common.web.error.SystemErrorRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stores unexpected failures in {@code SYSTEM_ERROR_LOG}.
 *
 * <p>Runs in its own transaction ({@code REQUIRES_NEW}) so that an aborted business
 * transaction still leaves the evidence behind. Failures are swallowed and logged to the
 * file appender: a database outage must not turn one error into two.</p>
 */
public class JpaSystemErrorRecorder implements SystemErrorRecorder {

    private static final Logger fallbackLog = LoggerFactory.getLogger("SYSTEM_ERROR_FALLBACK");

    private final SystemErrorLogRepository repository;

    public JpaSystemErrorRecorder(SystemErrorLogRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(SystemErrorRecord record) {
        try {
            repository.save(new SystemErrorLog(
                    record.errorCode(),
                    record.exceptionClass(),
                    record.errorMessage(),
                    record.stackTrace(),
                    record.requestUri(),
                    record.httpMethod(),
                    record.userId(),
                    record.correlationId(),
                    record.requestTimestamp()));
        } catch (RuntimeException persistenceFailure) {
            // Last resort: the file log. Never rethrow, never recurse.
            fallbackLog.error("Could not persist system error [correlationId={}, code={}]: {}",
                    record.correlationId(), record.errorCode(), persistenceFailure.getMessage());
            fallbackLog.error("Original failure: {} - {}", record.exceptionClass(), record.errorMessage());
        }
    }
}
