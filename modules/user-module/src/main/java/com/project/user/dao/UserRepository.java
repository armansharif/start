package com.project.user.dao;

import com.project.common.persistence.dao.BaseJpaRepository;
import com.project.user.entity.User;

import java.util.Optional;

/**
 * Spring Data repository for users. Only lookups used by the module are declared here.
 */
public interface UserRepository extends BaseJpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByMobile(String mobile);
}
