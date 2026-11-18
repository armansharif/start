package com.project.user.interface_.service;

import com.project.common.core.paging.PageResult;
import com.project.user.dto.request.CreateUserRequest;
import com.project.user.dto.request.UpdateUserRequest;
import com.project.user.dto.response.UserResponse;
import com.project.user.entity.UserStatus;
import com.project.user.search.UserSearchModel;
import com.project.user.view.UserListItemView;

import java.util.Set;

/**
 * User use cases.
 *
 * <p>Controllers depend on this interface only; the implementation, the DAO and the
 * persistence technology stay behind it.</p>
 */
public interface UserService {

    UserResponse create(CreateUserRequest request);

    UserResponse update(Long id, UpdateUserRequest request);

    UserResponse getById(Long id);

    void changeStatus(Long id, UserStatus status);

    void assignRoles(Long id, Set<String> roleCodes);

    void delete(Long id);

    /** Entity oriented search (JPA specifications). */
    PageResult<UserResponse> search(UserSearchModel searchModel);

    /** Report oriented search (plain SQL + row mapping). */
    PageResult<UserListItemView> report(UserSearchModel searchModel);
}
