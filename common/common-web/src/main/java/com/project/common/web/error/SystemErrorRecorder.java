package com.project.common.web.error;

/**
 * Port for storing unexpected failures.
 *
 * <p>Implemented in {@code common-logging} (database + async + fallback to file logging).
 * Implementations must never propagate an exception: failing to log an error must not
 * replace the original failure.</p>
 */
public interface SystemErrorRecorder {

    void record(SystemErrorRecord record);
}
