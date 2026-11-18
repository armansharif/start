package com.project.common.logging.audit;

import com.project.common.persistence.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * A recorded business operation worth auditing.
 *
 * <p>Auditing is selective on purpose: recording every write produces a table nobody can
 * use. Modules decide which operations matter and call {@link AuditRecorder}.</p>
 */
@Entity
@Table(name = "AUDIT_LOG")
public class AuditLog extends BaseEntity {

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "OPERATION", nullable = false, length = 100)
    private String operation;

    @Column(name = "ENTITY_NAME", nullable = false, length = 100)
    private String entityName;

    @Column(name = "ENTITY_ID", length = 100)
    private String entityId;

    @Column(name = "OLD_VALUE", length = 4000)
    private String oldValue;

    @Column(name = "NEW_VALUE", length = 4000)
    private String newValue;

    @Column(name = "EVENT_TIMESTAMP", nullable = false)
    private Instant eventTimestamp;

    @Column(name = "IP_ADDRESS", length = 45)
    private String ipAddress;

    @Column(name = "CORRELATION_ID", length = 100)
    private String correlationId;

    protected AuditLog() {
        // for JPA
    }

    public AuditLog(Long userId,
                    String operation,
                    String entityName,
                    String entityId,
                    String oldValue,
                    String newValue,
                    Instant eventTimestamp,
                    String ipAddress,
                    String correlationId) {
        this.userId = userId;
        this.operation = operation;
        this.entityName = entityName;
        this.entityId = entityId;
        this.oldValue = truncate(oldValue);
        this.newValue = truncate(newValue);
        this.eventTimestamp = eventTimestamp;
        this.ipAddress = ipAddress;
        this.correlationId = correlationId;
    }

    private static String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 4000 ? value : value.substring(0, 4000);
    }

    public Long getUserId() {
        return userId;
    }

    public String getOperation() {
        return operation;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public Instant getEventTimestamp() {
        return eventTimestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getCorrelationId() {
        return correlationId;
    }
}
