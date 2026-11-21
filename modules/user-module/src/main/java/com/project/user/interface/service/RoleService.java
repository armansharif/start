package com.project.user.interface_.service;

import com.project.user.dto.response.PermissionResponse;
import com.project.user.dto.response.RolePermissionsResponse;
import com.project.user.dto.response.RoleResponse;

import java.util.List;
import java.util.Set;

/**
 * Role catalogue and role/permission use cases.
 */
public interface RoleService {

    /** All assignable roles, ordered by code. */
    List<RoleResponse> listAll();

    /** All permissions known to the platform, ordered by code. */
    List<PermissionResponse> listPermissions();

    /** One role with the permission codes currently attached to it. */
    RolePermissionsResponse permissionsOf(String roleCode);

    /** Replaces the whole permission set of a role and returns the saved state. */
    RolePermissionsResponse replacePermissions(String roleCode, Set<String> permissionCodes);
}
