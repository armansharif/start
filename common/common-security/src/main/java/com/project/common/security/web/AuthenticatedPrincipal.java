package com.project.common.security.web;

import java.util.Set;

/**
 * Principal stored in the security context for an authenticated request.
 *
 * <p>Contains only identifiers and roles: no entity, no lazy state, nothing that could
 * leak between requests.</p>
 */
public record AuthenticatedPrincipal(Long userId, String username, Set<String> roles) {

    public AuthenticatedPrincipal {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }
}
