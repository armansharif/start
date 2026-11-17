package com.project.common.security.error;

import com.project.common.core.error.ErrorCode;

/**
 * Authentication/authorization error codes.
 *
 * <p>Authentication messages stay generic on purpose: they never reveal whether a
 * username exists or which part of the credential was wrong.</p>
 */
public enum SecurityErrorCodes implements ErrorCode {

    INVALID_CREDENTIALS("AUTH-1001", "error.auth.invalidCredentials", "Invalid username or password"),
    ACCOUNT_DISABLED("AUTH-1002", "error.auth.accountDisabled", "Account is disabled"),
    ACCOUNT_LOCKED("AUTH-1003", "error.auth.accountLocked", "Account is temporarily locked"),
    INVALID_REFRESH_TOKEN("AUTH-1004", "error.auth.invalidRefreshToken", "Invalid refresh token"),
    EXPIRED_REFRESH_TOKEN("AUTH-1005", "error.auth.expiredRefreshToken", "Refresh token has expired"),
    INVALID_ACCESS_TOKEN("AUTH-1006", "error.auth.invalidAccessToken", "Invalid or expired access token");

    private final String code;
    private final String messageKey;
    private final String defaultMessage;

    SecurityErrorCodes(String code, String messageKey, String defaultMessage) {
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
