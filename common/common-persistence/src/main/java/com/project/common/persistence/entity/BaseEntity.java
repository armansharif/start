package com.project.common.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

/**
 * Base class of every persistent entity.
 *
 * <p>Deliberately minimal: identifier, audit columns and optimistic locking. Business
 * modules add their own columns; soft delete, tenant ids or other behaviour are opt-in
 * and must not be pushed into this class.</p>
 *
 * <p>Time columns hold {@link Instant} (UTC). MySQL stores them as {@code DATETIME(6)}
 * and Hibernate is configured with {@code hibernate.jdbc.time_zone=UTC}, so no implicit
 * conversion happens in the driver.</p>
 *
 * <p>Identifier strategy is {@code IDENTITY}: simple, native in MySQL and available in
 * Oracle 12c+ as well. See {@code docs/persistence-architecture.md} for the trade-offs.</p>
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, updatable = false)
    private Long id;

    @CreatedDate
    @Column(name = "CREATED_DATE", nullable = false, updatable = false)
    private Instant createdDate;

    @LastModifiedDate
    @Column(name = "MODIFIED_DATE", nullable = false)
    private Instant modifiedDate;

    @CreatedBy
    @Column(name = "CREATED_BY", nullable = false, updatable = false)
    private Long createdBy;

    @LastModifiedBy
    @Column(name = "MODIFIED_BY", nullable = false)
    private Long modifiedBy;

    /**
     * Optimistic locking counter. Set to 0 by Hibernate on insert and incremented on
     * every update; a mismatching value fails the update with an
     * {@code OptimisticLockException} instead of silently overwriting data.
     */
    @Version
    @Column(name = "VERSION", nullable = false)
    private Long version;

    public Long getId() {
        return id;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public Instant getModifiedDate() {
        return modifiedDate;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public Long getModifiedBy() {
        return modifiedBy;
    }

    public Long getVersion() {
        return version;
    }

    public boolean isNew() {
        return id == null;
    }

    /**
     * Equality is based on the identifier only, and only for persisted instances.
     * Two transient entities are never equal, which keeps unsaved instances usable in
     * sets without surprising collisions.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || !getClass().equals(other.getClass())) {
            return false;
        }
        BaseEntity that = (BaseEntity) other;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : Objects.hash(getClass(), id);
    }
}
