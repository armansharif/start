package com.project.common.security.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Login credentials.
 *
 * <p>Password length is only bounded to reject absurd payloads; password policy belongs
 * to user management, not to login.</p>
 */
public record LoginRequest(
        @NotBlank(message = "{validation.username.required}")
        @Size(max = 100, message = "{validation.username.size}")
        String username,

        @NotBlank(message = "{validation.password.required}")
        @Size(max = 200, message = "{validation.password.size}")
        String password) {
}
