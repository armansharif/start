package com.project.common.datetime.validator;

import com.project.common.datetime.model.PersianDate;
import com.project.common.datetime.parser.PersianDateParser;
import com.project.common.datetime.converter.Time4JPersianDateConverter;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.time.LocalDate;

/**
 * Class level validation of a date range: both boundaries must be valid Persian dates
 * (when stored as strings) and the start must not be after the end.
 *
 * <p>The properties may be {@code String} (Persian date) or {@code LocalDate} (already
 * converted). Missing values are skipped, so the annotation can sit on a search model
 * where the range is optional.</p>
 */
@Documented
@Constraint(validatedBy = ValidPersianDateRange.Validator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPersianDateRange {

    String message() default "{error.datetime.invalidPersianDateRange}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /** Name of the property holding the start of the range. */
    String from() default "fromDate";

    /** Name of the property holding the end of the range. */
    String to() default "toDate";

    class Validator implements ConstraintValidator<ValidPersianDateRange, Object> {

        private final PersianDateParser parser =
                new PersianDateParser(new Time4JPersianDateConverter());

        private String fromProperty;
        private String toProperty;

        @Override
        public void initialize(ValidPersianDateRange annotation) {
            this.fromProperty = annotation.from();
            this.toProperty = annotation.to();
        }

        @Override
        public boolean isValid(Object value, ConstraintValidatorContext context) {
            if (value == null) {
                return true;
            }
            LocalDate from;
            LocalDate to;
            try {
                from = readDate(value, fromProperty);
                to = readDate(value, toProperty);
            } catch (IllegalArgumentException ex) {
                return false;
            }
            return from == null || to == null || !to.isBefore(from);
        }

        private LocalDate readDate(Object bean, String property) {
            Object raw = readProperty(bean, property);
            if (raw == null) {
                return null;
            }
            if (raw instanceof LocalDate date) {
                return date;
            }
            if (raw instanceof String text) {
                if (text.isBlank()) {
                    return null;
                }
                PersianDate persianDate = parser.parse(text, property);
                return parserDateToGregorian(persianDate);
            }
            throw new IllegalArgumentException("Unsupported date property type: " + raw.getClass());
        }

        private LocalDate parserDateToGregorian(PersianDate persianDate) {
            return parserConverter().toGregorian(persianDate);
        }

        private com.project.common.datetime.converter.PersianDateConverter parserConverter() {
            return new Time4JPersianDateConverter();
        }

        private static Object readProperty(Object bean, String property) {
            for (Class<?> type = bean.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
                try {
                    Field field = type.getDeclaredField(property);
                    field.setAccessible(true);
                    return field.get(bean);
                } catch (NoSuchFieldException ex) {
                    // continue with the superclass
                } catch (IllegalAccessException ex) {
                    throw new IllegalArgumentException("Cannot read property: " + property, ex);
                }
            }
            throw new IllegalArgumentException("Unknown date range property: " + property);
        }
    }
}
