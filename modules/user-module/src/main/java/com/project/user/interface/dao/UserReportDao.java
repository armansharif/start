package com.project.user.interface_.dao;

import com.project.common.core.paging.PageResult;
import com.project.user.search.UserSearchModel;
import com.project.user.view.UserListItemView;

/**
 * Read contract for the user list report.
 *
 * <p>Separated from {@link UserDao} because it is a different read model, not a different
 * way to load the entity: the service picks the contract, the configuration picks the
 * implementation.</p>
 */
public interface UserReportDao {

    PageResult<UserListItemView> search(UserSearchModel searchModel);
}
