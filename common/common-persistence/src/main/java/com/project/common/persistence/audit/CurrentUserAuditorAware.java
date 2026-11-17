package com.project.common.persistence.audit;

import com.project.common.core.security.CurrentUserProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * Feeds {@code @CreatedBy}/{@code @LastModifiedBy} with the acting user.
 *
 * <p>The provider is optional: in a test slice or a batch job without security there is
 * no authenticated user, and the system id is recorded instead of failing the insert.</p>
 */
public class CurrentUserAuditorAware implements AuditorAware<Long> {

    private final ObjectProvider<CurrentUserProvider> currentUserProvider;

    public CurrentUserAuditorAware(ObjectProvider<CurrentUserProvider> currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public Optional<Long> getCurrentAuditor() {
        CurrentUserProvider provider = currentUserProvider.getIfAvailable();
        if (provider == null) {
            return Optional.of(CurrentUserProvider.SYSTEM_USER_ID);
        }
        return Optional.of(provider.currentUserIdOrSystem());
    }
}
