package com.project.user.dto.request;

import com.project.user.entity.UserStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request to change an account status.
 */
public record ChangeStatusRequest(
        @NotNull(message = "{validation.status.required}")
        UserStatus status) {
}
