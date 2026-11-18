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

    /**
     * Resolves a Bean Validation message template that is still a raw {@code {key}}.
     *
     * <p>Constraint annotations carry a message key, not a sentence. When the validation
     * provider hands the template back unresolved (for example a custom constraint whose
     * key the interpolator could not read), the client would otherwise receive
     * {@code {error.datetime.invalidPersianDate}} instead of readable text.</p>
     *
     * @param template message or message key wrapped in braces
     * @return resolved message, or the input when it is not a template
     */
    public String resolveTemplate(String template) {
        if (template == null || template.length() < 3 || !template.startsWith("{") || !template.endsWith("}")) {
            return template;
        }
        String key = template.substring(1, template.length() - 1);
        return messageSource.getMessage(key, null, template, LocaleContextHolder.getLocale());
    }
}
