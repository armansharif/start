package com.project.common.security.token;

import java.time.Instant;

/**
 * A refresh token as handed to the client. The raw value is never persisted and never
 * logged; only its hash is stored.
 */
public record IssuedRefreshToken(String tokenValue, Instant expiresAt, String familyId) {
}
