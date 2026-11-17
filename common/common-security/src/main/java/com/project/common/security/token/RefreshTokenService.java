package com.project.common.security.token;

import com.project.common.core.error.BusinessException;
import com.project.common.security.config.SecurityProperties;
import com.project.common.security.error.SecurityErrorCodes;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Issues, rotates and revokes refresh tokens.
 *
 * <p>Rotation rules:</p>
 * <ul>
 *   <li>every refresh consumes the presented token and issues a new one in the same family;</li>
 *   <li>presenting an already consumed token is treated as theft: the whole family is revoked;</li>
 *   <li>an expired token is rejected and the family is left untouched.</li>
 * </ul>
 */
public class RefreshTokenService {

    private final RefreshTokenStore store;
    private final SecurityProperties properties;
    private final Clock clock;

    public RefreshTokenService(RefreshTokenStore store, SecurityProperties properties, Clock clock) {
        this.store = Objects.requireNonNull(store, "store must not be null");
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public IssuedRefreshToken issue(Long userId) {
        return issue(userId, UUID.randomUUID().toString());
    }

    public IssuedRefreshToken issue(Long userId, String familyId) {
        Objects.requireNonNull(userId, "userId must not be null");
        String rawToken = TokenHash.generateRawToken();
        Instant expiresAt = Instant.now(clock).plus(properties.getJwt().getRefreshTokenTtl());

        RefreshToken token = new RefreshToken(userId, familyId, TokenHash.of(rawToken), expiresAt);
        store.save(token);
        return new IssuedRefreshToken(rawToken, expiresAt, familyId);
    }

    /**
     * @return the user id owning the token plus the newly issued replacement
     * @throws BusinessException when the token is unknown, revoked, reused or expired
     */
    public RotatedRefreshToken rotate(String rawToken) {
        RefreshToken stored = store.findByTokenHash(TokenHash.of(rawToken))
                .orElseThrow(() -> new BusinessException(SecurityErrorCodes.INVALID_REFRESH_TOKEN));

        Instant now = Instant.now(clock);

        if (stored.isRevoked()) {
            revokeFamily(stored.getFamilyId(), now);
            throw new BusinessException(SecurityErrorCodes.INVALID_REFRESH_TOKEN);
        }
        if (stored.isExpired(now)) {
            throw new BusinessException(SecurityErrorCodes.EXPIRED_REFRESH_TOKEN);
        }

        stored.revoke(now);
        store.save(stored);

        IssuedRefreshToken replacement = issue(stored.getUserId(), stored.getFamilyId());
        return new RotatedRefreshToken(stored.getUserId(), replacement);
    }

    /**
     * Revokes a single token (logout). Unknown or already revoked values are ignored:
     * logout must never fail and must not reveal whether a token existed.
     */
    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        store.findByTokenHash(TokenHash.of(rawToken)).ifPresent(token -> {
            token.revoke(Instant.now(clock));
            store.save(token);
        });
    }

    public void revokeFamily(String familyId, Instant when) {
        List<RefreshToken> family = store.findByFamilyId(familyId);
        family.forEach(token -> token.revoke(when));
        store.saveAll(family);
    }

    /**
     * @param userId user owning the rotated token
     * @param token  the replacement token to send to the client
     */
    public record RotatedRefreshToken(Long userId, IssuedRefreshToken token) {
    }
}
