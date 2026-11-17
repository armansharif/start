package com.project.common.security.bruteforce;

import com.project.common.core.error.BusinessException;
import com.project.common.security.config.SecurityProperties;
import com.project.common.security.error.SecurityErrorCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryBruteForceGuardTest {

    private SecurityProperties properties;
    private BruteForceGuard guard;

    @BeforeEach
    void setUp() {
        properties = new SecurityProperties();
        properties.getBruteForce().setMaxAttempts(3);
        properties.getBruteForce().setLockDuration(Duration.ofMinutes(15));
        properties.getBruteForce().setWindow(Duration.ofMinutes(15));
        guard = new InMemoryBruteForceGuard(properties, Clock.systemUTC());
    }

    @Test
    void locksAfterConfiguredNumberOfFailures() {
        String key = "reza|127.0.0.1";

        guard.recordFailure(key);
        guard.recordFailure(key);
        assertThatCode(() -> guard.assertNotLocked(key)).doesNotThrowAnyException();

        guard.recordFailure(key);

        assertThatThrownBy(() -> guard.assertNotLocked(key))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(SecurityErrorCodes.ACCOUNT_LOCKED);
    }

    @Test
    void resetClearsFailures() {
        String key = "reza|127.0.0.1";
        guard.recordFailure(key);
        guard.recordFailure(key);
        guard.recordFailure(key);

        guard.reset(key);

        assertThatCode(() -> guard.assertNotLocked(key)).doesNotThrowAnyException();
    }

    @Test
    void isDisabledByConfiguration() {
        properties.getBruteForce().setEnabled(false);
        String key = "reza|127.0.0.1";

        for (int i = 0; i < 10; i++) {
            guard.recordFailure(key);
        }

        assertThatCode(() -> guard.assertNotLocked(key)).doesNotThrowAnyException();
    }
}
