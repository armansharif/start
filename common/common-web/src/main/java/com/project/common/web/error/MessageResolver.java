package com.project.common.web.error;

import com.project.common.core.error.ErrorCode;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Objects;

/**
 * Resolves an {@link ErrorCode} to a localized message.
 *
 * <p>Messages live in {@code messages/messages_*.properties}; the code itself never
 * carries user facing text, so the same failure can be shown in Persian or English
 * without touching the business code.</p>
 */
public class MessageResolver {

    private final MessageSource messageSource;

    public MessageResolver(MessageSource messageSource) {
        this.messageSource = Objects.requireNonNull(messageSource, "messageSource must not be null");
    }

    public String resolve(ErrorCode errorCode, Object... args) {
        return messageSource.getMessage(errorCode.messageKey(), args,
                errorCode.defaultMessage(), LocaleContextHolder.getLocale());
    }
}
