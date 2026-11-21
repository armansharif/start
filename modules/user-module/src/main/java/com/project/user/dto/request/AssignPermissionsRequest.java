package com.project.user.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Request replacing the permission set of a role.
 */
public record AssignPermissionsRequest(
        @NotNull(message = "{validation.permissions.required}")
        @Size(max = 100, message = "{validation.permissions.size}")
        Set<String> permissions) {
}
