package com.project.common.security.jwt;

import com.project.common.security.authentication.AuthUser;
import com.project.common.security.config.SecurityProperties;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Issues and decodes access tokens.
 *
 * <p>Tokens are stateless and short lived. Roles and permissions are embedded so that a
 * request does not need a database round trip; the price is that a permission change is
 * visible only after the token expires (15 minutes by default) or when the token is
 * revoked. That trade-off is documented in {@code docs/security.md}.</p>
 */
public class JwtTokenService {

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final SecurityProperties properties;
    private final Clock clock;

    public JwtTokenService(JwtEncoder encoder,
                           JwtDecoder decoder,
                           SecurityProperties properties,
                           Clock clock) {
        this.encoder = Objects.requireNonNull(encoder, "encoder must not be null");
        this.decoder = Objects.requireNonNull(decoder, "decoder must not be null");
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public IssuedAccessToken issue(AuthUser user) {
        Instant issuedAt = Instant.now(clock);
        Instant expiresAt = issuedAt.plus(properties.getJwt().getAccessTokenTtl());
        String tokenId = UUID.randomUUID().toString();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getJwt().getIssuer())
                .subject(String.valueOf(user.id()))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .id(tokenId)
                .claim("username", user.username())
                .claim("roles", List.copyOf(user.roles()))
                .claim("perms", List.copyOf(user.permissions()))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        Jwt jwt = encoder.encode(JwtEncoderParameters.from(header, claims));
        return new IssuedAccessToken(jwt.getTokenValue(), expiresAt, tokenId);
    }

    /**
     * @throws org.springframework.security.oauth2.jwt.JwtException when the token is
     *         malformed, not signed by this issuer, or expired
     */
    public Jwt decode(String tokenValue) {
        return decoder.decode(tokenValue);
    }
}
