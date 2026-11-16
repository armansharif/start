package com.project.common.core.error;

import java.util.List;

/**
 * Thrown when request data violates a business validation rule that Bean Validation
 * cannot express (for example a cross-field or state-dependent rule).
 *
 * <p>Field-level Bean Validation failures are translated by the web layer from
 * {@code MethodArgumentNotValidException} instead of being re-thrown as this type.</p>
 */
public class ValidationException extends BusinessException {

    private final transient List<ErrorItem> errors;

    public ValidationException(ErrorCode errorCode, List<ErrorItem> errors, Object... messageArgs) {
        super(errorCode, messageArgs);
        this.errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public ValidationException(String field, ErrorCode errorCode) {
        super(errorCode, field);
        this.errors = List.of(new ErrorItem(errorCode.code(), field, errorCode.defaultMessage()));
    }

    public List<ErrorItem> getErrors() {
        return errors;
    }
}
