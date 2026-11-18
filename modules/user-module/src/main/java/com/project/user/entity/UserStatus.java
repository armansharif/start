package com.project.user.entity;

/**
 * Lifecycle state of a user account.
 *
 * <p>Authentication maps it as: {@code ACTIVE} can sign in, {@code INACTIVE} is disabled,
 * {@code LOCKED} is administratively blocked. The mapping lives in one place (the
 * authentication adapter), not in every caller.</p>
 */
public enum UserStatus {
    ACTIVE,
    INACTIVE,
    LOCKED
}
