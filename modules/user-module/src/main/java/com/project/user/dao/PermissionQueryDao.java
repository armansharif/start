package com.project.user.dao;

import java.util.Set;

/**
 * Reads the authorities of a user.
 *
 * <p>Implemented with a single JDBC join instead of walking entities: it is the natural
 * shape of the data and avoids three round trips plus mapping code.</p>
 */
public interface PermissionQueryDao {

    Set<String> roleCodesOf(Long userId);

    Set<String> permissionCodesOf(Long userId);
}
