package com.project.common.security.authentication;

import com.project.common.core.error.BusinessException;
import com.project.common.security.bruteforce.BruteForceGuard;
import com.project.common.security.bruteforce.InMemoryBruteForceGuard;
import com.project.common.security.config.SecurityProperties;
import com.project.common.security.error.SecurityErrorCodes;
import com.project.common.security.token.IssuedRefreshToken;
import com.project.common.security.token.RefreshToken;
import com.project.common.security.token.RefreshTokenService;
import com.project.common.security.token.RefreshTokenStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import com.project.common.security.jwt.JwtTokenService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticationServiceTest {

    private static final String RAW_PASSWORD = "Str0ng!Passw0rd";
    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private Map<String, AuthUser> usersByUsername;
    private AuthenticationService service;
    private SecurityProperties properties;

    @BeforeEach
    void setUp() {
        String hash = passwordEncoder.encode(RAW_PASSWORD);
        usersByUsername = new HashMap<>();
        usersByUsername.put("reza", new AuthUser(7L, "reza", hash, true, false,
                Set.of("ADMIN"), Set.of("user:read")));
        usersByUsername.put("disabled", new AuthUser(8L, "disabled", hash, false, false,
                Set.of("USER"), Set.of()));

        properties = new SecurityProperties();
        properties.getJwt().setSecret(SECRET);
        properties.getJwt().setAccessTokenTtl(Duration.ofMinutes(15));
        properties.getJwt().setRefreshTokenTtl(Duration.ofDays(30));
        properties.getBruteForce().setMaxAttempts(3);

        Clock clock = Clock.systemUTC();
        JwtTokenService jwtTokenService = jwtTokenService(clock);
        RefreshTokenService refreshTokenService = new RefreshTokenService(
                new InMemoryStore(), properties, clock);
        BruteForceGuard guard = new InMemoryBruteForceGuard(properties, clock);

        service = new AuthenticationService(new FakeUserPort(usersByUsername), passwordEncoder,
                jwtTokenService, refreshTokenService, guard);
    }

    @Test
    void logsInAndIssuesBothTokens() {
        TokenPair tokens = service.login("reza", RAW_PASSWORD, "127.0.0.1", "junit");

        assertThat(tokens.accessToken()).isNotBlank();
        assertThat(tokens.refreshToken()).isNotBlank();
        assertThat(tokens.tokenType()).isEqualTo("Bearer");
        assertThat(tokens.accessTokenExpiresAt()).isAfter(tokens.accessTokenExpiresAt().minusSeconds(1));
    }

    @Test
    void rejectsWrongPasswordWithoutRevealingTheReason() {
        assertThatThrownBy(() -> service.login("reza", "wrong", "127.0.0.1", "junit"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(SecurityErrorCodes.INVALID_CREDENTIALS);
    }

    @Test
    void rejectsUnknownUserWithTheSameCode() {
        assertThatThrownBy(() -> service.login("nobody", RAW_PASSWORD, "127.0.0.1", "junit"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(SecurityErrorCodes.INVALID_CREDENTIALS);
    }

    @Test
    void rejectsDisabledAccount() {
        assertThatThrownBy(() -> service.login("disabled", RAW_PASSWORD, "127.0.0.1", "junit"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(SecurityErrorCodes.ACCOUNT_DISABLED);
    }

    @Test
    void locksAccountAfterTooManyFailures() {
        for (int attempt = 0; attempt < 3; attempt++) {
            assertThatThrownBy(() -> service.login("reza", "wrong", "10.0.0.1", "junit"))
                    .isInstanceOf(BusinessException.class);
        }

        assertThatThrownBy(() -> service.login("reza", RAW_PASSWORD, "10.0.0.1", "junit"))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(SecurityErrorCodes.ACCOUNT_LOCKED);
    }

    @Test
    void refreshIssuesNewPairAndRevokesTheOldToken() {
        TokenPair tokens = service.login("reza", RAW_PASSWORD, "127.0.0.1", "junit");

        TokenPair refreshed = service.refresh(tokens.refreshToken());

        assertThat(refreshed.accessToken()).isNotBlank();
        assertThat(refreshed.refreshToken()).isNotEqualTo(tokens.refreshToken());
        assertThatThrownBy(() -> service.refresh(tokens.refreshToken()))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(SecurityErrorCodes.INVALID_REFRESH_TOKEN);
    }

    @Test
    void logoutRevokesTheRefreshToken() {
        TokenPair tokens = service.login("reza", RAW_PASSWORD, "127.0.0.1", "junit");

        service.logout(tokens.refreshToken());

        assertThatThrownBy(() -> service.refresh(tokens.refreshToken()))
                .isInstanceOf(BusinessException.class);
    }

    private JwtTokenService jwtTokenService(Clock clock) {
        SecretKey key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder encoder = NimbusJwtEncoder.withSecretKey(key).build();
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.getJwt().getIssuer()));
        return new JwtTokenService(encoder, decoder, properties, clock);
    }

    private record FakeUserPort(Map<String, AuthUser> users) implements AuthenticationUserPort {

        @Override
        public Optional<AuthUser> findByUsername(String username) {
            return Optional.ofNullable(users.get(username));
        }

        @Override
        public Optional<AuthUser> findById(Long userId) {
            return users.values().stream().filter(user -> user.id().equals(userId)).findFirst();
        }
    }

    private static final class InMemoryStore implements RefreshTokenStore {

        private final Map<String, RefreshToken> byHash = new HashMap<>();

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
            List<RefreshToken> family = new ArrayList<>();
            byHash.values().stream()
                    .filter(token -> token.getFamilyId().equals(familyId))
                    .forEach(family::add);
            return family;
        }
    }
}
