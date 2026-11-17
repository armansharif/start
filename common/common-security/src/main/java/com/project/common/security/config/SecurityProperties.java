package com.project.common.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Security configuration. Secrets are never written in this file's defaults: the JWT
 * secret must come from the environment ({@code APP_JWT_SECRET}).
 */
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private final Jwt jwt = new Jwt();
    private final Cors cors = new Cors();
    private final BruteForce bruteForce = new BruteForce();

    public Jwt getJwt() {
        return jwt;
    }

    public Cors getCors() {
        return cors;
    }

    public BruteForce getBruteForce() {
        return bruteForce;
    }

    public static class Jwt {

        /**
         * Value of the {@code iss} claim; also validated on every incoming token.
         * Kept URI shaped because OAuth2 JWT tooling expects an absolute URI issuer.
         */
        private String issuer = "https://backend-platform.local";

        /** HMAC secret, at least 32 bytes. Supplied by the environment, never committed. */
        private String secret;

        /** Access tokens are short lived on purpose: revocation is the fallback, not the rule. */
        private Duration accessTokenTtl = Duration.ofMinutes(15);

        /** Refresh tokens are long lived but rotated on every use. */
        private Duration refreshTokenTtl = Duration.ofDays(30);

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public Duration getAccessTokenTtl() {
            return accessTokenTtl;
        }

        public void setAccessTokenTtl(Duration accessTokenTtl) {
            this.accessTokenTtl = accessTokenTtl;
        }

        public Duration getRefreshTokenTtl() {
            return refreshTokenTtl;
        }

        public void setRefreshTokenTtl(Duration refreshTokenTtl) {
            this.refreshTokenTtl = refreshTokenTtl;
        }
    }

    public static class Cors {

        /** Exact origins allowed to call the API. Empty means "no cross-origin browser client". */
        private List<String> allowedOrigins = new ArrayList<>();

        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

        private List<String> allowedHeaders = List.of("Authorization", "Content-Type", "Accept", "X-Request-Id",
                "Idempotency-Key");

        private boolean allowCredentials = false;

        private Duration maxAge = Duration.ofHours(1);

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }

        public List<String> getAllowedMethods() {
            return allowedMethods;
        }

        public void setAllowedMethods(List<String> allowedMethods) {
            this.allowedMethods = allowedMethods;
        }

        public List<String> getAllowedHeaders() {
            return allowedHeaders;
        }

        public void setAllowedHeaders(List<String> allowedHeaders) {
            this.allowedHeaders = allowedHeaders;
        }

        public boolean isAllowCredentials() {
            return allowCredentials;
        }

        public void setAllowCredentials(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
        }

        public Duration getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(Duration maxAge) {
            this.maxAge = maxAge;
        }
    }

    public static class BruteForce {

        private boolean enabled = true;

        /** Consecutive failures before the account/key is temporarily locked. */
        private int maxAttempts = 5;

        /** How long a locked key stays locked. */
        private Duration lockDuration = Duration.ofMinutes(15);

        /** Observation window for counting failures. */
        private Duration window = Duration.ofMinutes(15);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public Duration getLockDuration() {
            return lockDuration;
        }

        public void setLockDuration(Duration lockDuration) {
            this.lockDuration = lockDuration;
        }

        public Duration getWindow() {
            return window;
        }

        public void setWindow(Duration window) {
            this.window = window;
        }
    }
}
