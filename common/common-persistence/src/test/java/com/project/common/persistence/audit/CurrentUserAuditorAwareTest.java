package com.project.common.persistence.audit;

import com.project.common.core.security.CurrentUserProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrentUserAuditorAwareTest {

    @SuppressWarnings("unchecked")
    private static ObjectProvider<CurrentUserProvider> providerReturning(CurrentUserProvider provider) {
        ObjectProvider<CurrentUserProvider> objectProvider = mock(ObjectProvider.class);
        when(objectProvider.getIfAvailable()).thenReturn(provider);
        return objectProvider;
    }

    @Test
    void usesAuthenticatedUser() {
        CurrentUserProvider provider = mock(CurrentUserProvider.class);
        when(provider.currentUserIdOrSystem()).thenReturn(42L);

        CurrentUserAuditorAware auditorAware = new CurrentUserAuditorAware(providerReturning(provider));

        assertThat(auditorAware.getCurrentAuditor()).contains(42L);
    }

    @Test
    void fallsBackToSystemUserWithoutProvider() {
        CurrentUserAuditorAware auditorAware = new CurrentUserAuditorAware(providerReturning(null));

        assertThat(auditorAware.getCurrentAuditor()).contains(CurrentUserProvider.SYSTEM_USER_ID);
    }

    @Test
    void fallsBackToSystemUserWhenAnonymous() {
        CurrentUserProvider provider = mock(CurrentUserProvider.class);
        when(provider.currentUserIdOrSystem()).thenReturn(CurrentUserProvider.SYSTEM_USER_ID);
        when(provider.currentUserId()).thenReturn(Optional.empty());

        CurrentUserAuditorAware auditorAware = new CurrentUserAuditorAware(providerReturning(provider));

        assertThat(auditorAware.getCurrentAuditor()).contains(0L);
    }
}
