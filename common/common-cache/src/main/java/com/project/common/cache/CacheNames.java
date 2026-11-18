package com.project.common.cache;

/**
 * Names of the caches used by the platform.
 *
 * <p>Centralising names avoids the classic bug where one module writes to
 * {@code "users"} and another evicts {@code "user"}. Each name has its own TTL in
 * configuration.</p>
 */
public final class CacheNames {

    /** User permissions per user id; evicted on any role/permission change. */
    public static final String USER_PERMISSIONS = "user-permissions";

    /** Single user details by id. */
    public static final String USER_BY_ID = "user-by-id";

    /** Reference data that rarely changes (permission catalogue). */
    public static final String PERMISSION_CATALOG = "permission-catalog";

    private CacheNames() {
    }
}
