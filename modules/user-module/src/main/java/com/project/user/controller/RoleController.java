package com.project.user.controller;

import com.project.common.web.api.ApiResponse;
import com.project.user.dto.request.AssignPermissionsRequest;
import com.project.user.dto.response.RolePermissionsResponse;
import com.project.user.dto.response.RoleResponse;
import com.project.user.interface_.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Role catalogue and role/permission endpoints.
 */
@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public ApiResponse<List<RoleResponse>> list() {
        return ApiResponse.of(roleService.listAll());
    }

    @GetMapping("/{code}/permissions")
    @PreAuthorize("hasAuthority('user:read')")
    public ApiResponse<RolePermissionsResponse> permissions(@PathVariable String code) {
        return ApiResponse.of(roleService.permissionsOf(code));
    }

    @PutMapping("/{code}/permissions")
    @PreAuthorize("hasAuthority('user:assign-role')")
    public ApiResponse<RolePermissionsResponse> replacePermissions(
            @PathVariable String code,
            @Valid @RequestBody AssignPermissionsRequest request) {
        return ApiResponse.of(roleService.replacePermissions(code, request.permissions()),
                "Permissions saved");
    }
}
