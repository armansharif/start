package com.project.user.dto.response;

/**
 * One entry of the permission catalogue.
 */
public record PermissionResponse(
        Long id,
        String code,
        String title) {
}
