package com.project.common.security.authentication;

import com.project.common.core.error.BusinessException;
import com.project.common.security.bruteforce.BruteForceGuard;
import com.project.common.security.config.SecurityProperties;
import com.project.common.security.error.SecurityErrorCodes;
import com.project.common.security.jwt.IssuedAccessToken;
import com.project.common.security.jwt.JwtTokenService;
import com.project.common.security.token.IssuedRefreshToken;
import com.project.common.security.token.RefreshTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Login, refresh and logout.
 *
 * <p>Failure modes deliberately collapse into one message: an unknown user, a wrong
 * password and a locked key all produce {@code AUTH-1001} on the client side, while the
 * server records the real reason in logs. This prevents account enumeration.</p>
 */
public class AuthenticationService {

    private final AuthenticationUserPort users;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final BruteForceGuard bruteForceGuard;

    public AuthenticationService(AuthenticationUserPort users,
                                 PasswordEncoder passwordEncoder,
                                 JwtTokenService jwtTokenService,
                                 RefreshTokenService refreshTokenService,
                                 BruteForceGuard bruteForceGuard) {
        this.users = Objects.requireNonNull(users, "users must not be null");
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "passwordEncoder must not be null");
        this.jwtTokenService = Objects.requireNonNull(jwtTokenService, "jwtTokenService must not be null");
        this.refreshTokenService = Objects.requireNonNull(refreshTokenService, "refreshTokenService must not be null");
        this.bruteForceGuard = Objects.requireNonNull(bruteForceGuard, "bruteForceGuard must not be null");
    }

    @Transactional
    public TokenPair login(String username, String rawPassword, String ipAddress, String userAgent) {
        String guardKey = guardKey(username, ipAddress);
        bruteForceGuard.assertNotLocked(guardKey);

        AuthUser user = users.findByUsername(username).orElse(null);
        if (user == null) {
            bruteForceGuard.recordFailure(guardKey);
            throw new BusinessException(SecurityErrorCodes.INVALID_CREDENTIALS);
        }
        if (!passwordEncoder.matches(rawPassword, user.passwordHash())) {
            bruteForceGuard.recordFailure(guardKey);
            throw new BusinessException(SecurityErrorCodes.INVALID_CREDENTIALS);
        }
        if (!user.enabled()) {
            throw new BusinessException(SecurityErrorCodes.ACCOUNT_DISABLED);
        }
        if (user.locked()) {
            throw new BusinessException(SecurityErrorCodes.ACCOUNT_LOCKED);
        }

        bruteForceGuard.reset(guardKey);
        return issueTokens(user);
    }

    @Transactional
    public TokenPair refresh(String rawRefreshToken) {
        RefreshTokenService.RotatedRefreshToken rotated = refreshTokenService.rotate(rawRefreshToken);

        AuthUser user = users.findById(rotated.userId())
                .orElseThrow(() -> new BusinessException(SecurityErrorCodes.INVALID_REFRESH_TOKEN));
        if (!user.enabled()) {
            throw new BusinessException(SecurityErrorCodes.ACCOUNT_DISABLED);
        }
        if (user.locked()) {
            throw new BusinessException(SecurityErrorCodes.ACCOUNT_LOCKED);
        }

        IssuedAccessToken accessToken = jwtTokenService.issue(user);
        IssuedRefreshToken refreshToken = rotated.token();
        return new TokenPair(accessToken.tokenValue(), refreshToken.tokenValue(), "Bearer",
                accessToken.expiresAt(), refreshToken.expiresAt());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }

    private TokenPair issueTokens(AuthUser user) {
        IssuedAccessToken accessToken = jwtTokenService.issue(user);
        IssuedRefreshToken refreshToken = refreshTokenService.issue(user.id());
        return new TokenPair(accessToken.tokenValue(), refreshToken.tokenValue(), "Bearer",
                accessToken.expiresAt(), refreshToken.expiresAt());
    }

    private static String guardKey(String username, String ipAddress) {
        String normalizedUsername = username == null ? "" : username.trim().toLowerCase();
        return normalizedUsername + "|" + (ipAddress == null ? "unknown" : ipAddress);
    }

    /** Exposes the configured TTL for clients that want to schedule a silent refresh. */
    public static long accessTokenTtlSeconds(SecurityProperties properties) {
        return properties.getJwt().getAccessTokenTtl().toSeconds();
    }
}
