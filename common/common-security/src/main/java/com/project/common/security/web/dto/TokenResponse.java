package com.project.common.security.web.dto;

import com.project.common.security.authentication.TokenPair;

import java.time.Instant;

/**
 * Tokens returned to the client. Refresh tokens are returned here exactly once; they are
 * never included in any other response and never logged.
 */
public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt) {

    public static TokenResponse from(TokenPair pair) {
        return new TokenResponse(pair.accessToken(), pair.refreshToken(), pair.tokenType(),
                pair.accessTokenExpiresAt(), pair.refreshTokenExpiresAt());
    }
}
