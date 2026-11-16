package com.project.common.core.error;

/**
 * Thrown when the caller cannot be authenticated (missing, expired, revoked or
 * otherwise unusable credentials).
 *
 * <p>The message is deliberately generic: it must never reveal whether a username
 * exists or which part of the credential was wrong.</p>
 */
public class AuthenticationException extends BusinessException {

    public AuthenticationException() {
        super(CommonErrorCode.AUTHENTICATION_REQUIRED);
    }

    public AuthenticationException(ErrorCode errorCode, Object... messageArgs) {
        super(errorCode, messageArgs);
    }
}
