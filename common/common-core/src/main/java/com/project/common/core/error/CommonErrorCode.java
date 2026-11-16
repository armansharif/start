package com.project.common.core.error;

/**
 * Error codes that belong to the platform itself rather than to a business module.
 *
 * <p>Business modules define their own enum implementing {@link ErrorCode} with a
 * module prefix (for example {@code USER-1001}); they must not add values here.</p>
 */
public enum CommonErrorCode implements ErrorCode {

    INVALID_ARGUMENT("COMMON-1001", "error.common.invalidArgument", "Invalid request argument"),
    VALIDATION_FAILED("COMMON-1002", "error.common.validationFailed", "Validation failed"),
    RESOURCE_NOT_FOUND("COMMON-1004", "error.common.resourceNotFound", "Resource not found"),
    DUPLICATE_RESOURCE("COMMON-1005", "error.common.duplicateResource", "Resource already exists"),
    AUTHENTICATION_REQUIRED("COMMON-1401", "error.common.authenticationRequired", "Authentication required"),
    ACCESS_DENIED("COMMON-1403", "error.common.accessDenied", "Access denied"),
    RATE_LIMIT_EXCEEDED("COMMON-1429", "error.common.rateLimitExceeded", "Too many requests"),
    SEARCH_INVALID_FIELD("COMMON-1501", "error.search.invalidField", "Field is not searchable"),
    SEARCH_INVALID_OPERATOR("COMMON-1502", "error.search.invalidOperator", "Operator is not allowed for this field"),
    SEARCH_LIMIT_EXCEEDED("COMMON-1503", "error.search.limitExceeded", "Search request exceeds the allowed limits"),
    PAGINATION_LIMIT_EXCEEDED("COMMON-1504", "error.search.paginationLimitExceeded", "Requested page size is too large"),
    INVALID_DATE_FORMAT("COMMON-1601", "error.datetime.invalidFormat", "Invalid date format"),
    INVALID_PERSIAN_DATE("COMMON-1602", "error.datetime.invalidPersianDate", "Invalid Persian date"),
    SYSTEM_ERROR("COMMON-5000", "error.common.systemError", "Unexpected system error");

    private final String code;
    private final String messageKey;
    private final String defaultMessage;

    CommonErrorCode(String code, String messageKey, String defaultMessage) {
        this.code = code;
        this.messageKey = messageKey;
        this.defaultMessage = defaultMessage;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String messageKey() {
        return messageKey;
    }

    @Override
    public String defaultMessage() {
        return defaultMessage;
    }
}
