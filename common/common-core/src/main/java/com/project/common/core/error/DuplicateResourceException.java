package com.project.common.core.error;

/**
 * Thrown when a uniqueness rule is violated. Persistence implementations translate
 * {@code DataIntegrityViolationException} into this type so that services and
 * controllers stay independent of the database.
 */
public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(ErrorCode errorCode, Object... messageArgs) {
        super(errorCode, messageArgs);
    }
}
