package com.project.common.security.web;

import com.project.common.core.security.CurrentUserProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Resolves the acting user from the Spring Security context.
 *
 * <p>This is the implementation of the {@code CurrentUserProvider} port that persistence
 * auditing and the audit trail use; it is why no lower layer has to depend on Spring
 * Security.</p>
 */
public class SecurityContextCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Optional<Long> currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedPrincipal authenticatedPrincipal) {
            return Optional.ofNullable(authenticatedPrincipal.userId());
        }
        return Optional.empty();
    }
}
