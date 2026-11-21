package com.project.user.dto.response;

/**
 * Role as returned to clients.
 *
 * <p>Only the catalogue fields are exposed; permission wiring stays server side until a
 * screen needs it.</p>
 */
public record RoleResponse(
        Long id,
        String code,
        String title) {
}
