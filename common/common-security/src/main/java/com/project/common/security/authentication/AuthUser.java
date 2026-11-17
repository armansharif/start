package com.project.common.security.authentication;

import java.util.Set;

/**
 * What the security layer needs to know about a user in order to authenticate it.
 *
 * <p>Deliberately narrow: no entity, no business fields. The user module maps its
 * entity to this view behind {@link AuthenticationUserPort}.</p>
 *
 * @param id           user identifier
 * @param username     login name
 * @param passwordHash encoded password (BCrypt/Argon2 via {@code DelegatingPasswordEncoder})
 * @param enabled      whether the account may authenticate
 * @param locked       whether the account is administratively locked
 * @param roles        role names, without the {@code ROLE_} prefix
 * @param permissions  permission strings such as {@code user:create}
 */
public record AuthUser(
        Long id,
        String username,
        String passwordHash,
        boolean enabled,
        boolean locked,
        Set<String> roles,
        Set<String> permissions) {

    public AuthUser {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }
}
