package com.project.user.error;

import com.project.common.core.error.ErrorCode;

/**
 * Error codes owned by the user module.
 */
public enum UserErrorCodes implements ErrorCode {

    DUPLICATE_USERNAME("USER-1001", "error.user.duplicateUsername", "Username is already taken"),
    DUPLICATE_MOBILE("USER-1002", "error.user.duplicateMobile", "Mobile number is already registered"),
    UNKNOWN_ROLE("USER-1003", "error.user.unknownRole", "One or more roles do not exist"),
    INVALID_BIRTH_DATE("USER-1004", "error.user.invalidBirthDate", "Birth date is not a valid Persian date"),
    USER_NOT_FOUND("USER-1005", "error.user.notFound", "User not found");

    private final String code;
    private final String messageKey;
    private final String defaultMessage;

    UserErrorCodes(String code, String messageKey, String defaultMessage) {
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
