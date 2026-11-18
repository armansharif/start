package com.project.user.security;

import com.project.common.security.authentication.AuthUser;
import com.project.user.dao.PermissionQueryDao;
import com.project.user.dao.UserRepository;
import com.project.user.entity.User;
import com.project.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserAuthenticationAdapterTest {

    private UserRepository userRepository;
    private PermissionQueryDao permissionQueryDao;
    private UserAuthenticationAdapter adapter;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        permissionQueryDao = mock(PermissionQueryDao.class);
        adapter = new UserAuthenticationAdapter(userRepository, permissionQueryDao);
    }

    @Test
    void resolvesAuthoritiesAndNormalisesTheUsername() {
        User user = new User("ali", "{bcrypt}hash", "Ali");
        when(userRepository.findByUsernameIgnoreCase("ali")).thenReturn(Optional.of(user));
        when(permissionQueryDao.roleCodesOf(any())).thenReturn(Set.of("ADMIN"));
        when(permissionQueryDao.permissionCodesOf(any())).thenReturn(Set.of("user:read"));

        AuthUser authUser = adapter.findByUsername("  ali ").orElseThrow();

        assertThat(authUser.username()).isEqualTo("ali");
        assertThat(authUser.passwordHash()).isEqualTo("{bcrypt}hash");
        assertThat(authUser.enabled()).isTrue();
        assertThat(authUser.locked()).isFalse();
        assertThat(authUser.roles()).containsExactly("ADMIN");
        assertThat(authUser.permissions()).containsExactly("user:read");
    }

    @Test
    void translatesLockedStatusIntoTheSecurityFlag() {
        User user = new User("ali", "{bcrypt}hash", "Ali");
        user.setStatus(UserStatus.LOCKED);
        when(userRepository.findByUsernameIgnoreCase("ali")).thenReturn(Optional.of(user));

        AuthUser authUser = adapter.findByUsername("ali").orElseThrow();

        assertThat(authUser.locked()).isTrue();
        assertThat(authUser.enabled()).isFalse();
    }

    @Test
    void inactiveUserIsDisabled() {
        User user = new User("ali", "{bcrypt}hash", "Ali");
        user.setStatus(UserStatus.INACTIVE);
        when(userRepository.findByUsernameIgnoreCase("ali")).thenReturn(Optional.of(user));

        AuthUser authUser = adapter.findByUsername("ali").orElseThrow();

        assertThat(authUser.enabled()).isFalse();
        assertThat(authUser.locked()).isFalse();
    }

    @Test
    void blankUsernameResolvesToNothing() {
        assertThat(adapter.findByUsername("   ")).isEmpty();
        assertThat(adapter.findById(null)).isEmpty();
    }
}
