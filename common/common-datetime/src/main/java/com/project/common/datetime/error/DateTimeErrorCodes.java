package com.project.common.datetime.error;

import com.project.common.core.error.ErrorCode;

/**
 * Date/time specific error codes owned by {@code common-datetime}.
 */
public enum DateTimeErrorCodes implements ErrorCode {

    INVALID_PERSIAN_DATE("COMMON-1602", "error.datetime.invalidPersianDate", "Invalid Persian date"),
    INVALID_PERSIAN_DATE_RANGE("COMMON-1603", "error.datetime.invalidPersianDateRange",
            "Invalid Persian date range"),
    INVALID_DATE_RANGE("COMMON-1604", "error.datetime.invalidDateRange", "Invalid date range"),
    INVALID_TIMEZONE("COMMON-1605", "error.datetime.invalidTimezone", "Invalid time zone");

    private final String code;
    private final String messageKey;
    private final String defaultMessage;

    DateTimeErrorCodes(String code, String messageKey, String defaultMessage) {
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
