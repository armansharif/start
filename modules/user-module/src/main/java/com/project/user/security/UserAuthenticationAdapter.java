package com.project.user.security;

import com.project.common.security.authentication.AuthUser;
import com.project.common.security.authentication.AuthenticationUserPort;
import com.project.user.dao.PermissionQueryDao;
import com.project.user.dao.UserRepository;
import com.project.user.entity.User;
import com.project.user.entity.UserStatus;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Connects the security layer to the user module.
 *
 * <p>This is the only place where the account status is translated into the flags the
 * security layer understands; every other component sees {@code enabled}/{@code locked}.</p>
 */
public class UserAuthenticationAdapter implements AuthenticationUserPort {

    private final UserRepository userRepository;
    private final PermissionQueryDao permissionQueryDao;

    public UserAuthenticationAdapter(UserRepository userRepository, PermissionQueryDao permissionQueryDao) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.permissionQueryDao = Objects.requireNonNull(permissionQueryDao, "permissionQueryDao must not be null");
    }

    @Override
    public Optional<AuthUser> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByUsernameIgnoreCase(username.trim()).map(this::toAuthUser);
    }

    @Override
    public Optional<AuthUser> findById(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return userRepository.findById(userId).map(this::toAuthUser);
    }

    private AuthUser toAuthUser(User user) {
        Set<String> roles = permissionQueryDao.roleCodesOf(user.getId());
        Set<String> permissions = permissionQueryDao.permissionCodesOf(user.getId());
        return new AuthUser(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.getStatus() == UserStatus.ACTIVE,
                user.getStatus() == UserStatus.LOCKED,
                roles,
                permissions);
    }
}
