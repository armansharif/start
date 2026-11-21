package com.project.user.dao;

import com.project.common.persistence.dao.BaseJpaRepository;
import com.project.user.entity.Permission;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for the permission catalogue.
 */
public interface PermissionRepository extends BaseJpaRepository<Permission, Long> {

    Optional<Permission> findByCode(String code);

    List<Permission> findByCodeIn(Collection<String> codes);
}
