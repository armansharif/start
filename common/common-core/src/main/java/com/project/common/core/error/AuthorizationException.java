package com.project.common.core.error;

/**
 * Thrown when an authenticated caller is not allowed to perform the operation.
 */
public class AuthorizationException extends BusinessException {

    public AuthorizationException() {
        super(CommonErrorCode.ACCESS_DENIED);
    }

    public AuthorizationException(ErrorCode errorCode, Object... messageArgs) {
        super(errorCode, messageArgs);
    }
}
