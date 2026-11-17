package com.project.common.security.authentication;

import java.time.Instant;

/**
 * Access and refresh token handed to a client after a successful login or refresh.
 */
public record TokenPair(
        String accessToken,
        String refreshToken,
        String tokenType,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt) {

    public TokenPair {
        tokenType = tokenType == null ? "Bearer" : tokenType;
    }
}
