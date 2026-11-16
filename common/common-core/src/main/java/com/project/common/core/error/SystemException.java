package com.project.common.core.error;

/**
 * Thrown when a failure is not part of the business contract (infrastructure,
 * unexpected state, defects).
 *
 * <p>Unlike {@link BusinessException}, a system error is recorded in
 * {@code SYSTEM_ERROR_LOG} with its stack trace, and the client receives only the
 * error code and the correlation id.</p>
 */
public class SystemException extends RuntimeException {

    private final ErrorCode errorCode;

    public SystemException(Throwable cause) {
        super(cause == null ? CommonErrorCode.SYSTEM_ERROR.defaultMessage() : cause.getMessage(), cause);
        this.errorCode = CommonErrorCode.SYSTEM_ERROR;
    }

    public SystemException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
