package com.project.user.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Request replacing the role set of a user.
 */
public record AssignRolesRequest(
        @NotNull(message = "{validation.roles.required}")
        @Size(max = 20, message = "{validation.roles.size}")
        Set<String> roles) {
}
