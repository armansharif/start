package com.project.user.service.impl;

import com.project.common.core.error.BusinessException;
import com.project.common.logging.audit.AuditRecorder;
import com.project.user.dao.PermissionRepository;
import com.project.user.dao.RolePermissionRepository;
import com.project.user.dao.RoleRepository;
import com.project.user.dto.response.PermissionResponse;
import com.project.user.dto.response.RolePermissionsResponse;
import com.project.user.dto.response.RoleResponse;
import com.project.user.entity.Permission;
import com.project.user.entity.Role;
import com.project.user.entity.RolePermission;
import com.project.user.error.UserErrorCodes;
import com.project.user.interface_.service.RoleService;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Read only role catalogue plus the role/permission wiring.
 *
 * <p>The role set is small and changes rarely, so it is returned whole instead of
 * paginated; the screens that assign roles need the complete list anyway.</p>
 */
public class RoleServiceImpl implements RoleService {

    private static final String ENTITY_NAME = "ROLE";

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final AuditRecorder auditRecorder;

    public RoleServiceImpl(RoleRepository roleRepository,
                           PermissionRepository permissionRepository,
                           RolePermissionRepository rolePermissionRepository,
                           AuditRecorder auditRecorder) {
        this.roleRepository = Objects.requireNonNull(roleRepository, "roleRepository must not be null");
        this.permissionRepository = Objects.requireNonNull(permissionRepository,
                "permissionRepository must not be null");
        this.rolePermissionRepository = Objects.requireNonNull(rolePermissionRepository,
                "rolePermissionRepository must not be null");
        this.auditRecorder = Objects.requireNonNull(auditRecorder, "auditRecorder must not be null");
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> listAll() {
        return roleRepository.findAll(Sort.by(Sort.Direction.ASC, "code")).stream()
                .map(role -> new RoleResponse(role.getId(), role.getCode(), role.getTitle()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> listPermissions() {
        return permissionRepository.findAll(Sort.by(Sort.Direction.ASC, "code")).stream()
                .map(permission -> new PermissionResponse(permission.getId(), permission.getCode(),
                        permission.getTitle()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RolePermissionsResponse permissionsOf(String roleCode) {
        Role role = requireRole(roleCode);
        return toResponse(role);
    }

    @Override
    @Transactional
    public RolePermissionsResponse replacePermissions(String roleCode, Set<String> permissionCodes) {
        Role role = requireRole(roleCode);
        String before = currentPermissionCodes(role.getId()).toString();

        rolePermissionRepository.deleteByRoleId(role.getId());
        // Hibernate runs inserts before deletes within one flush, so the old links must be
        // gone before the new ones are written; otherwise re-saving a permission that the
        // role already had violates the unique key UK_ROLE_PERMISSION.
        rolePermissionRepository.flush();

        if (permissionCodes != null && !permissionCodes.isEmpty()) {
            List<Permission> permissions = permissionRepository.findByCodeIn(permissionCodes);
            if (permissions.size() != permissionCodes.size()) {
                throw new BusinessException(UserErrorCodes.UNKNOWN_PERMISSION);
            }
            permissions.forEach(permission ->
                    rolePermissionRepository.save(new RolePermission(role.getId(), permission.getId())));
        }

        List<String> after = currentPermissionCodes(role.getId());
        auditRecorder.record("ROLE_PERMISSION_ASSIGN", ENTITY_NAME, String.valueOf(role.getId()),
                before, after.toString(), null);

        return new RolePermissionsResponse(role.getId(), role.getCode(), role.getTitle(), after);
    }

    // ------------------------------------------------------------------ helpers

    private Role requireRole(String roleCode) {
        return roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new BusinessException(UserErrorCodes.ROLE_NOT_FOUND));
    }

    private List<String> currentPermissionCodes(Long roleId) {
        List<Long> permissionIds = rolePermissionRepository.findByRoleId(roleId).stream()
                .map(RolePermission::getPermissionId)
                .toList();
        if (permissionIds.isEmpty()) {
            return List.of();
        }
        return permissionRepository.findAllById(permissionIds).stream()
                .sorted((left, right) -> left.getCode().compareTo(right.getCode()))
                .map(Permission::getCode)
                .toList();
    }

    private RolePermissionsResponse toResponse(Role role) {
        return new RolePermissionsResponse(role.getId(), role.getCode(), role.getTitle(),
                currentPermissionCodes(role.getId()));
    }
}
