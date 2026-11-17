package com.project.common.search.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Model level search metadata.
 *
 * <p>The common date range of {@code BaseSearchModel} ({@code fromDate}/{@code toDate})
 * has to be bound to a real column; that binding is declared here once per model instead
 * of hard coding it in every DAO.</p>
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchableModel {

    /** Column the common {@code fromDate}/{@code toDate} range applies to (SQL strategy). */
    String dateColumn() default "";

    /** Java property the common {@code fromDate}/{@code toDate} range applies to (JPA strategy). */
    String dateProperty() default "";

    /** Persistence type of {@link #dateColumn()}; decides how bounds are bound. */
    DateColumnType dateColumnType() default DateColumnType.TIMESTAMP;

    /** Default comparison used by {@code addFilterByObject} for fields without an explicit operator. */
    String defaultOperator() default "";

    enum DateColumnType {
        /** Column stores a calendar date ({@code DATE}); bounds are {@code LocalDate}. */
        DATE,
        /** Column stores a moment ({@code DATETIME}); bounds are half-open instants. */
        TIMESTAMP
    }
}
