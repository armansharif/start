package com.project.common.security.jwt;

import com.project.common.security.authentication.AuthUser;
import com.project.common.security.config.SecurityProperties;
import com.project.common.security.token.TokenHash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    private SecurityProperties properties;
    private JwtTokenService service;

    @BeforeEach
    void setUp() {
        properties = new SecurityProperties();
        properties.getJwt().setSecret(SECRET);
        properties.getJwt().setAccessTokenTtl(Duration.ofMinutes(15));
        service = serviceWith(secretKey(SECRET), properties);
    }

    @Test
    void issuesTokenWithExpectedClaims() {
        AuthUser user = new AuthUser(7L, "reza", "{noop}x", true, false,
                Set.of("ADMIN"), Set.of("user:read", "user:create"));

        IssuedAccessToken issued = service.issue(user);
        Jwt jwt = service.decode(issued.tokenValue());

        assertThat(jwt.getSubject()).isEqualTo("7");
        assertThat(jwt.getClaimAsString("username")).isEqualTo("reza");
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("ADMIN");
        assertThat(jwt.getClaimAsStringList("perms")).containsExactlyInAnyOrder("user:read", "user:create");
        assertThat(jwt.getId()).isEqualTo(issued.tokenId());
        assertThat(jwt.getIssuedAt()).isNotNull();
        assertThat(jwt.getExpiresAt()).isAfter(Instant.now().minusSeconds(5));
        assertThat(jwt.getClaimAsString("iss")).isEqualTo(properties.getJwt().getIssuer());
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        AuthUser user = new AuthUser(1L, "user", "{noop}x", true, false, Set.of(), Set.of());
        String token = service.issue(user).tokenValue();

        JwtTokenService other = serviceWith(secretKey("ffffffffffffffffffffffffffffffff"), properties);

        assertThatThrownBy(() -> other.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsExpiredToken() {
        // The encoder refuses expiresAt before issuedAt, so an already expired token is
        // produced with a valid ordering in the past.
        SecretKey key = secretKey(SECRET);
        JwtEncoder encoder = NimbusJwtEncoder.withSecretKey(key).build();
        Instant now = Instant.now();
        var claims = org.springframework.security.oauth2.jwt.JwtClaimsSet.builder()
                .issuer(properties.getJwt().getIssuer())
                .subject("1")
                .issuedAt(now.minusSeconds(7200))
                .expiresAt(now.minusSeconds(3600))
                .build();
        String expired = encoder.encode(org.springframework.security.oauth2.jwt.JwtEncoderParameters.from(
                org.springframework.security.oauth2.jwt.JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();

        assertThatThrownBy(() -> service.decode(expired)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsTokenFromAnotherIssuer() {
        AuthUser user = new AuthUser(1L, "user", "{noop}x", true, false, Set.of(), Set.of());
        String token = service.issue(user).tokenValue();

        SecurityProperties otherIssuer = new SecurityProperties();
        otherIssuer.getJwt().setSecret(SECRET);
        otherIssuer.getJwt().setIssuer("someone-else");
        JwtTokenService otherService = serviceWith(secretKey(SECRET), otherIssuer);

        assertThatThrownBy(() -> otherService.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void refreshTokenHashesAreStableAndOpaque() {
        String raw = TokenHash.generateRawToken();

        assertThat(TokenHash.of(raw)).isEqualTo(TokenHash.of(raw)).hasSize(64).doesNotContain(raw);
        assertThat(TokenHash.generateRawToken()).isNotEqualTo(raw);
    }

    private static JwtTokenService serviceWith(SecretKey key, SecurityProperties props) {
        JwtEncoder encoder = NimbusJwtEncoder.withSecretKey(key).build();
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(props.getJwt().getIssuer()));
        return new JwtTokenService(encoder, (JwtDecoder) decoder, props, Clock.systemUTC());
    }

    private static SecretKey secretKey(String secret) {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
