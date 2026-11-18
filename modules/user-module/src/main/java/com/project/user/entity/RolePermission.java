package com.project.user.entity;

import com.project.common.persistence.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Link between a role and a permission.
 */
@Entity
@Table(name = "ROLE_PERMISSION")
public class RolePermission extends BaseEntity {

    @Column(name = "ROLE_ID", nullable = false, updatable = false)
    private Long roleId;

    @Column(name = "PERMISSION_ID", nullable = false, updatable = false)
    private Long permissionId;

    protected RolePermission() {
        // for JPA
    }

    public RolePermission(Long roleId, Long permissionId) {
        this.roleId = roleId;
        this.permissionId = permissionId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public Long getPermissionId() {
        return permissionId;
    }
}
