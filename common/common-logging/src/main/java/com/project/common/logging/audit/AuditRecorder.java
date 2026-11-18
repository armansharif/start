package com.project.common.logging.audit;

import com.project.common.core.security.CurrentUserProvider;
import com.project.common.logging.correlation.CorrelationIdFilter;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/**
 * Records audit entries for the operations a module declares as auditable.
 *
 * <p>Called explicitly from services (no annotation magic): the call site makes it clear
 * what is audited and with which before/after value, which is exactly the information a
 * later investigation needs.</p>
 */
public class AuditRecorder {

    private final AuditLogRepository repository;
    private final CurrentUserProvider currentUserProvider;
    private final Clock clock;

    public AuditRecorder(AuditLogRepository repository,
                         CurrentUserProvider currentUserProvider,
                         Clock clock) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.currentUserProvider = Objects.requireNonNull(currentUserProvider, "currentUserProvider must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    /**
     * @param operation  e.g. {@code USER_UPDATE}
     * @param entityName domain entity name, e.g. {@code USER}
     * @param entityId   identifier of the affected row
     * @param oldValue   serialized previous state (already masked where needed), may be null
     * @param newValue   serialized new state, may be null
     * @param ipAddress  client address, may be null for jobs
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String operation,
                       String entityName,
                       String entityId,
                       String oldValue,
                       String newValue,
                       String ipAddress) {
        repository.save(new AuditLog(
                currentUserProvider.currentUserIdOrSystem(),
                operation,
                entityName,
                entityId,
                oldValue,
                newValue,
                Instant.now(clock),
                ipAddress,
                MDC.get(CorrelationIdFilter.MDC_KEY)));
    }
}
