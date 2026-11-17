package com.project.common.security.token;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Database backed {@link RefreshTokenStore}.
 *
 * <p>Writes happen inside the caller's transaction (the authentication service is
 * transactional), so a rotation that fails half way leaves no dangling state.</p>
 */
public class JpaRefreshTokenStore implements RefreshTokenStore {

    private final RefreshTokenRepository repository;

    public JpaRefreshTokenStore(RefreshTokenRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        return repository.save(token);
    }

    @Override
    public void saveAll(List<RefreshToken> tokens) {
        repository.saveAll(tokens);
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash);
    }

    @Override
    public List<RefreshToken> findByFamilyId(String familyId) {
        return repository.findByFamilyId(familyId);
    }
}
