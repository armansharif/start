package com.project.common.security.jwt;

import java.time.Instant;

/**
 * A freshly signed access token.
 *
 * @param tokenValue signed JWT
 * @param expiresAt  expiry instant (UTC)
 * @param tokenId    {@code jti} claim, used for revocation
 */
public record IssuedAccessToken(String tokenValue, Instant expiresAt, String tokenId) {
}
