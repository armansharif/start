package com.project.common.core.error;

/**
 * Contract for every error code exposed by the platform.
 *
 * <p>Implementations live in the module that owns the error (for example
 * {@code USER-1002}). A code is stable API: clients may switch on it, so it must
 * never be reused or renamed. The human readable text is never carried here; it
 * is resolved from {@code MessageSource} through {@link #messageKey()} so that
 * the same failure can be rendered in Persian and English.</p>
 *
 * <p>This type is intentionally free of Spring, JPA and Servlet dependencies: the
 * mapping from an error to an HTTP status belongs to the web layer.</p>
 */
public interface ErrorCode {

    /**
     * Stable, client-visible identifier, e.g. {@code COMMON-1001} or {@code USER-1002}.
     */
    String code();

    /**
     * Message resource key used to resolve the localized message.
     */
    String messageKey();

    /**
     * Default message used when no localized message exists for {@link #messageKey()}.
     */
    String defaultMessage();
}
