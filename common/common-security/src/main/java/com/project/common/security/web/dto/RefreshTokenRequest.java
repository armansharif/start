package com.project.common.security.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Refresh token presented to obtain a new token pair.
 */
public record RefreshTokenRequest(
        @NotBlank(message = "{validation.refreshToken.required}")
        String refreshToken) {
}
