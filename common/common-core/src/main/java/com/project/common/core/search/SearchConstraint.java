package com.project.common.core.search;

import java.util.List;
import java.util.Objects;

/**
 * One comparison inside a {@link SearchFilterModel}, for example
 * {@code customerGroupName LIKE 'gold'}.
 *
 * <p>For {@link MatchOperator#IS_NULL} and the other no-value operators the value
 * is {@code null}; for {@link MatchOperator#BETWEEN} it is a two-element list; for
 * {@code IN}/{@code NOT_IN} it is a collection. Validation of that shape happens in
 * the search engines, not here, so the model can also be built from JSON.</p>
 *
 * @param fieldValue compared value(s); may be {@code null} only for no-value operators
 * @param operator   comparison to apply
 */
public record SearchConstraint(Object fieldValue, MatchOperator operator) {

    public SearchConstraint {
        Objects.requireNonNull(operator, "operator must not be null");
    }

    public static SearchConstraint of(Object fieldValue, MatchOperator operator) {
        return new SearchConstraint(fieldValue, operator);
    }

    /** @return the value as an ordered pair, or {@code null} when the value is not pair shaped */
    @SuppressWarnings("unchecked")
    public List<Object> asPair() {
        if (fieldValue instanceof List<?> list && list.size() == 2) {
            return (List<Object>) list;
        }
        if (fieldValue instanceof Object[] array && array.length == 2) {
            return List.of(array[0], array[1]);
        }
        return null;
    }
}
