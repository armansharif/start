package com.project.user.controller;

import com.project.common.web.api.ApiResponse;
import com.project.user.dto.response.PermissionResponse;
import com.project.user.interface_.service.RoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Permission catalogue endpoint.
 *
 * <p>The catalogue is reference data: screens that edit a role's permissions need the
 * whole list to render one checkbox per permission.</p>
 */
@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {

    private final RoleService roleService;

    public PermissionController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public ApiResponse<List<PermissionResponse>> list() {
        return ApiResponse.of(roleService.listPermissions());
    }
}
