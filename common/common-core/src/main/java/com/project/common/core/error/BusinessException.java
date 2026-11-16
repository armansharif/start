package com.project.common.core.error;

/**
 * Base type for expected, business-level failures.
 *
 * <p>Business exceptions are part of the contract with the caller: they carry a
 * stable {@link ErrorCode} and are never treated as system errors (no entry in
 * the system error log, no stack trace to the client).</p>
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final transient Object[] messageArgs;

    public BusinessException(ErrorCode errorCode, Object... messageArgs) {
        super(errorCode.defaultMessage());
        this.errorCode = errorCode;
        this.messageArgs = messageArgs;
    }

    public BusinessException(ErrorCode errorCode, Throwable cause, Object... messageArgs) {
        super(errorCode.defaultMessage(), cause);
        this.errorCode = errorCode;
        this.messageArgs = messageArgs;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Object[] getMessageArgs() {
        return messageArgs == null ? new Object[0] : messageArgs.clone();
    }
}
