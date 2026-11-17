package com.project.common.search.annotation;

import com.project.common.core.search.MatchOperator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that a search model field may be used by clients.
 *
 * <p>Nothing is searchable or sortable unless it carries this annotation: the default
 * is closed, so a new field cannot leak into the public query surface by accident.</p>
 *
 * <p>{@code name} is what clients send, {@code column} is the physical column used by the
 * SQL strategy and {@code fieldName} (the Java field) is the property path used by the
 * JPA strategy. If {@code column} is missing the field is usable with JPA only.</p>
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Searchable {

    /** Public name accepted from clients; defaults to the Java field name. */
    String name() default "";

    /** Additional accepted name, for compatibility while renaming an API field. */
    String alias() default "";

    /** Physical column name used by the JDBC strategy. */
    String column() default "";

    /** Comparison applied when the client sends a bare value for this field. */
    MatchOperator operator() default MatchOperator.EQUALS;

    /** Whether clients may sort by this field. */
    boolean sortable() default false;

    /** Whether the free-text {@code keyword} also searches this field. */
    boolean keywordSearch() default false;
}
