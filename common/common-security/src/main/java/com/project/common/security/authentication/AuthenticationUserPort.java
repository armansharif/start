package com.project.common.security.authentication;

import java.util.Optional;

/**
 * Port through which the security layer loads credentials.
 *
 * <p>Implemented by the user module. Keeping it a port means {@code common-security}
 * knows nothing about the user entity, the user tables or the module that owns them.</p>
 */
public interface AuthenticationUserPort {

    Optional<AuthUser> findByUsername(String username);

    /** Loads the current roles and permissions of a user, used after a refresh. */
    Optional<AuthUser> findById(Long userId);
}
