package com.project.common.datetime.validator;

import com.project.common.datetime.parser.PersianDateParser;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;

/**
 * Validates that a string is a well formed Persian date ({@code 1405/08/24} or
 * {@code 1405-08-24}). {@code null} and blank values are considered valid; combine with
 * {@code @NotBlank} when the field is required.
 */
@Documented
@Constraint(validatedBy = ValidPersianDate.Validator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPersianDate {

    String message() default "{error.datetime.invalidPersianDate}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Validator implementation. It is instantiated by Hibernate Validator, so the parser
     * is created directly instead of being injected; the parser itself is stateless.
     */
    class Validator implements ConstraintValidator<ValidPersianDate, CharSequence> {

        private final PersianDateParser parser;

        public Validator() {
            this.parser = new PersianDateParser(new com.project.common.datetime.converter.Time4JPersianDateConverter());
        }

        @Override
        public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
            if (value == null || value.toString().isBlank()) {
                return true;
            }
            return parser.canParse(value.toString());
        }
    }
}
