package com.project.common.core.error;

/**
 * One entry of the {@code errors} array in an API error response.
 *
 * @param code    stable error code, e.g. {@code COMMON-1002}
 * @param field   offending field name, or {@code null} when the error is not field specific
 * @param message localized message, already resolved by the web layer
 */
public record ErrorItem(String code, String field, String message) {

    public static ErrorItem of(ErrorCode errorCode, String message) {
        return new ErrorItem(errorCode.code(), null, message);
    }

    public static ErrorItem of(ErrorCode errorCode, String field, String message) {
        return new ErrorItem(errorCode.code(), field, message);
    }
}
