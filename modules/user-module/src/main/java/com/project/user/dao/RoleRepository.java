package com.project.user.dao;

import com.project.common.persistence.dao.BaseJpaRepository;
import com.project.user.entity.Role;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for roles.
 */
public interface RoleRepository extends BaseJpaRepository<Role, Long> {

    Optional<Role> findByCode(String code);

    List<Role> findByCodeIn(Collection<String> codes);

    List<Role> findByIdIn(Collection<Long> ids);
}
