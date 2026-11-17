package com.project.common.security.token;

import com.project.common.core.error.BusinessException;
import com.project.common.security.config.SecurityProperties;
import com.project.common.security.error.SecurityErrorCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshTokenServiceTest {

    private InMemoryRefreshTokenStore store;
    private SecurityProperties properties;

    @BeforeEach
    void setUp() {
        store = new InMemoryRefreshTokenStore();
        properties = new SecurityProperties();
        properties.getJwt().setRefreshTokenTtl(Duration.ofDays(30));
    }

    @Test
    void rotationReplacesTokenAndKeepsFamily() {
        RefreshTokenService service = serviceAt(Instant.parse("2026-01-01T00:00:00Z"));
        IssuedRefreshToken initial = service.issue(5L);

        RefreshTokenService.RotatedRefreshToken rotated = service.rotate(initial.tokenValue());

        assertThat(rotated.userId()).isEqualTo(5L);
        assertThat(rotated.token().tokenValue()).isNotEqualTo(initial.tokenValue());
        assertThat(rotated.token().familyId()).isEqualTo(initial.familyId());
        assertThat(store.findByFamilyId(initial.familyId())).hasSize(2)
                .anyMatch(RefreshToken::isRevoked);
    }

    @Test
    void reuseOfConsumedTokenRevokesWholeFamily() {
        RefreshTokenService service = serviceAt(Instant.parse("2026-01-01T00:00:00Z"));
        IssuedRefreshToken initial = service.issue(5L);
        service.rotate(initial.tokenValue());

        assertThatThrownBy(() -> service.rotate(initial.tokenValue()))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(SecurityErrorCodes.INVALID_REFRESH_TOKEN);

        assertThat(store.findByFamilyId(initial.familyId())).allMatch(RefreshToken::isRevoked);
    }

    @Test
    void expiredTokenIsRejected() {
        RefreshTokenService issuer = serviceAt(Instant.parse("2026-01-01T00:00:00Z"));
        IssuedRefreshToken initial = issuer.issue(9L);

        RefreshTokenService later = serviceAt(Instant.parse("2026-03-01T00:00:00Z"));

        assertThatThrownBy(() -> later.rotate(initial.tokenValue()))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(SecurityErrorCodes.EXPIRED_REFRESH_TOKEN);
    }

    @Test
    void unknownTokenIsRejected() {
        RefreshTokenService service = serviceAt(Instant.parse("2026-01-01T00:00:00Z"));

        assertThatThrownBy(() -> service.rotate("does-not-exist"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void revokeIsIdempotentAndForgiving() {
        RefreshTokenService service = serviceAt(Instant.parse("2026-01-01T00:00:00Z"));
        IssuedRefreshToken initial = service.issue(3L);

        service.revoke(initial.tokenValue());
        service.revoke(initial.tokenValue());
        service.revoke(null);

        assertThatThrownBy(() -> service.rotate(initial.tokenValue()))
                .isInstanceOf(BusinessException.class);
    }

    private RefreshTokenService serviceAt(Instant instant) {
        return new RefreshTokenService(store, properties, Clock.fixed(instant, ZoneOffset.UTC));
    }

    /** Minimal store used to test rotation logic without a database. */
    private static final class InMemoryRefreshTokenStore implements RefreshTokenStore {

        private final Map<String, RefreshToken> byHash = new LinkedHashMap<>();

        @Override
        public RefreshToken save(RefreshToken token) {
            byHash.put(token.getTokenHash(), token);
            return token;
        }

        @Override
        public void saveAll(List<RefreshToken> tokens) {
            tokens.forEach(this::save);
        }

        @Override
        public Optional<RefreshToken> findByTokenHash(String tokenHash) {
            return Optional.ofNullable(byHash.get(tokenHash));
        }

        @Override
        public List<RefreshToken> findByFamilyId(String familyId) {
            List<RefreshToken> result = new ArrayList<>();
            byHash.values().stream()
                    .filter(token -> token.getFamilyId().equals(familyId))
                    .forEach(result::add);
            return result;
        }
    }
}
