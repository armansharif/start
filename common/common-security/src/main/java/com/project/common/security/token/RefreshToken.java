package com.project.common.security.token;

import com.project.common.persistence.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Stored refresh token.
 *
 * <p>Only the hash is persisted. Rotation keeps the same {@code familyId}, which allows
 * detecting reuse of an already rotated token and revoking the whole family (a sign that
 * the token was stolen).</p>
 */
@Entity
@Table(name = "REFRESH_TOKEN")
public class RefreshToken extends BaseEntity {

    @Column(name = "USER_ID", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "FAMILY_ID", nullable = false, updatable = false, length = 36)
    private String familyId;

    @Column(name = "TOKEN_HASH", nullable = false, updatable = false, length = 64)
    private String tokenHash;

    @Column(name = "EXPIRES_AT", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "REVOKED_AT")
    private Instant revokedAt;

    @Column(name = "IP_ADDRESS", length = 45)
    private String ipAddress;

    @Column(name = "USER_AGENT", length = 300)
    private String userAgent;

    protected RefreshToken() {
        // for JPA
    }

    public RefreshToken(Long userId, String familyId, String tokenHash, Instant expiresAt) {
        this.userId = userId;
        this.familyId = familyId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public void revoke(Instant when) {
        if (this.revokedAt == null) {
            this.revokedAt = when;
        }
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(Instant now) {
        return expiresAt.isBefore(now) || expiresAt.equals(now);
    }

    public void assignClientInfo(String ipAddress, String userAgent) {
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFamilyId() {
        return familyId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }
}
