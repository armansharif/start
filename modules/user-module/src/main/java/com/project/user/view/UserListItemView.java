package com.project.user.view;

import java.time.Instant;

/**
 * Row of the user list report.
 *
 * <p>Produced by the JDBC DAO and mapped explicitly with a {@code RowMapper}: this is the
 * type-safe replacement for {@code Map<String, Object>} results.</p>
 *
 * @param id          user id
 * @param username    login name
 * @param fullName    display name
 * @param mobile      mobile number
 * @param status      account status
 * @param createdDate creation instant (UTC)
 */
public record UserListItemView(
        Long id,
        String username,
        String fullName,
        String mobile,
        String status,
        Instant createdDate) {
}
