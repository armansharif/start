package com.project.user.dto.response;

import java.util.List;

/**
 * A role together with the permission codes currently assigned to it.
 */
public record RolePermissionsResponse(
        Long id,
        String code,
        String title,
        List<String> permissions) {
}
