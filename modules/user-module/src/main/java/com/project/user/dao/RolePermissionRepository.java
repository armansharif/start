package com.project.user.dao;

import com.project.common.persistence.dao.BaseJpaRepository;
import com.project.user.entity.RolePermission;

import java.util.List;

/**
 * Spring Data repository for role/permission links.
 */
public interface RolePermissionRepository extends BaseJpaRepository<RolePermission, Long> {

    List<RolePermission> findByRoleId(Long roleId);

    void deleteByRoleId(Long roleId);

    boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);
}
