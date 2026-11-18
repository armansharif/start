package com.project.user.interface_.dao;

import com.project.common.core.paging.PageResult;
import com.project.common.persistence.dao.BaseDao;
import com.project.user.entity.User;
import com.project.user.search.UserSearchModel;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Persistence contract for users.
 *
 * <p>Note the package name: {@code interface_} because {@code interface} is a Java
 * keyword. The service depends only on this contract.</p>
 */
public interface UserDao extends BaseDao<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByMobile(String mobile);

    /**
     * Entity oriented dynamic search resolved with JPA specifications.
     */
    PageResult<User> search(UserSearchModel searchModel);

    List<User> findAllByIdIn(Collection<Long> ids);
}
