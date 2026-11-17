package com.project.common.security.token;

import java.util.List;
import java.util.Optional;

/**
 * Storage abstraction for refresh tokens.
 *
 * <p>Keeping it an interface lets the rotation logic be tested without a database and
 * leaves room for a Redis backed implementation if the volume ever demands it.</p>
 */
public interface RefreshTokenStore {

    RefreshToken save(RefreshToken token);

    void saveAll(List<RefreshToken> tokens);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByFamilyId(String familyId);
}
