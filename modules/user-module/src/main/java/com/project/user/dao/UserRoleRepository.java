package com.project.user.dao;

import com.project.common.persistence.dao.BaseJpaRepository;
import com.project.user.entity.UserRole;

import java.util.Collection;
import java.util.List;

/**
 * Spring Data repository for user/role links.
 */
public interface UserRoleRepository extends BaseJpaRepository<UserRole, Long> {

    List<UserRole> findByUserId(Long userId);

    List<UserRole> findByUserIdIn(Collection<Long> userIds);

    void deleteByUserId(Long userId);

    void deleteByUserIdAndRoleId(Long userId, Long roleId);

    boolean existsByUserIdAndRoleId(Long userId, Long roleId);
}
