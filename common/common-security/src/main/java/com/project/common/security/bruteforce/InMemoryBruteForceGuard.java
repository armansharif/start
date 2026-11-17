package com.project.common.security.bruteforce;

import com.project.common.core.error.BusinessException;
import com.project.common.security.config.SecurityProperties;
import com.project.common.security.error.SecurityErrorCodes;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Single-instance brute force guard.
 *
 * <p>State is per JVM, so it protects a single node only. That is enough for development
 * and for a first deployment behind a single instance; the Redis implementation replaces
 * this class without touching the authentication code.</p>
 */
public class InMemoryBruteForceGuard implements BruteForceGuard {

    private final Map<String, Attempts> attemptsByKey = new ConcurrentHashMap<>();
    private final SecurityProperties properties;
    private final Clock clock;

    public InMemoryBruteForceGuard(SecurityProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public void assertNotLocked(String key) {
        if (!properties.getBruteForce().isEnabled()) {
            return;
        }
        Attempts attempts = attemptsByKey.get(key);
        if (attempts == null) {
            return;
        }
        Instant now = Instant.now(clock);
        Instant lockedUntil = attempts.lockedUntil;
        if (lockedUntil != null && lockedUntil.isAfter(now)) {
            throw new BusinessException(SecurityErrorCodes.ACCOUNT_LOCKED);
        }
    }

    @Override
    public void recordFailure(String key) {
        if (!properties.getBruteForce().isEnabled()) {
            return;
        }
        Instant now = Instant.now(clock);
        Duration window = properties.getBruteForce().getWindow();

        attemptsByKey.compute(key, (k, existing) -> {
            if (existing == null || existing.firstFailure.plus(window).isBefore(now)) {
                return new Attempts(1, now, null);
            }
            int failures = existing.failures + 1;
            Instant lockedUntil = existing.lockedUntil;
            if (failures >= properties.getBruteForce().getMaxAttempts()) {
                lockedUntil = now.plus(properties.getBruteForce().getLockDuration());
            }
            return new Attempts(failures, existing.firstFailure, lockedUntil);
        });
    }

    @Override
    public void reset(String key) {
        attemptsByKey.remove(key);
    }

    /**
     * Mutable counter kept in a single immutable snapshot; the map compute function
     * replaces it atomically, so no locking is needed.
     */
    private static final class Attempts {
        private final int failures;
        private final Instant firstFailure;
        private final Instant lockedUntil;

        private Attempts(int failures, Instant firstFailure, Instant lockedUntil) {
            this.failures = failures;
            this.firstFailure = firstFailure;
            this.lockedUntil = lockedUntil;
        }
    }
}
