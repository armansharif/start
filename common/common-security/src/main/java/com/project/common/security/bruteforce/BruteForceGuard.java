package com.project.common.security.bruteforce;

/**
 * Guards login against credential stuffing and brute force.
 *
 * <p>The implementation used here is in-process; a distributed implementation backed by
 * Redis is required when more than one instance serves traffic and will be added in the
 * rate limiting step. Both share this contract, so the authentication service does not
 * change.</p>
 */
public interface BruteForceGuard {

    /**
     * @throws com.project.common.core.error.BusinessException when the key is locked
     */
    void assertNotLocked(String key);

    void recordFailure(String key);

    void reset(String key);
}
