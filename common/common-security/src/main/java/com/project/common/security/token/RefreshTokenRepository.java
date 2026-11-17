package com.project.common.security.token;

import com.project.common.persistence.dao.BaseJpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for refresh tokens.
 */
public interface RefreshTokenRepository extends BaseJpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByFamilyId(String familyId);
}
