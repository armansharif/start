package com.project.common.core.security;

import java.util.Optional;

/**
 * Port for "who is acting right now".
 *
 * <p>It lives in {@code common-core} so that lower layers (persistence auditing,
 * audit trail) can ask for the current user without depending on Spring Security.
 * The implementation is provided by {@code common-security}.</p>
 */
public interface CurrentUserProvider {

    /** Identifier used for changes made by the system itself (jobs, migrations, startup). */
    long SYSTEM_USER_ID = 0L;

    /**
     * @return the authenticated user id, or empty when the call is anonymous or triggered by the system
     */
    Optional<Long> currentUserId();

    default Long currentUserIdOrSystem() {
        return currentUserId().orElse(SYSTEM_USER_ID);
    }
}
