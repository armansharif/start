package com.project.common.core.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Conditions applied to a single field, combined with {@link #getLogicalOperator()}.
 *
 * <p>Example: {@code fieldName = "customerGroupName"}, {@code logicalOperator = OR},
 * constraints {@code LIKE 'gold'} and {@code LIKE 'silver'}.</p>
 *
 * <p>Filters are combined with AND by the search engines in the current version;
 * nested groups are intentionally not supported yet.</p>
 */
public class SearchFilterModel {

    private final String fieldName;
    private LogicalOperator logicalOperator;
    private final List<SearchConstraint> constraints = new ArrayList<>();

    public SearchFilterModel(String fieldName) {
        this(fieldName, LogicalOperator.AND);
    }

    public SearchFilterModel(String fieldName, LogicalOperator logicalOperator) {
        this.fieldName = Objects.requireNonNull(fieldName, "fieldName must not be null");
        this.logicalOperator = logicalOperator == null ? LogicalOperator.AND : logicalOperator;
    }

    public static SearchFilterModel of(String fieldName, LogicalOperator logicalOperator,
                                       Object value, MatchOperator matchOperator) {
        SearchFilterModel filter = new SearchFilterModel(fieldName, logicalOperator);
        filter.addConstraint(value, matchOperator);
        return filter;
    }

    public SearchFilterModel addConstraint(Object value, MatchOperator matchOperator) {
        constraints.add(new SearchConstraint(value, matchOperator));
        return this;
    }

    public SearchFilterModel addConstraint(SearchConstraint constraint) {
        constraints.add(Objects.requireNonNull(constraint, "constraint must not be null"));
        return this;
    }

    public String getFieldName() {
        return fieldName;
    }

    public LogicalOperator getLogicalOperator() {
        return logicalOperator;
    }

    public void setLogicalOperator(LogicalOperator logicalOperator) {
        this.logicalOperator = logicalOperator == null ? LogicalOperator.AND : logicalOperator;
    }

    public List<SearchConstraint> getConstraints() {
        return List.copyOf(constraints);
    }

    public boolean isEmpty() {
        return constraints.isEmpty();
    }
}
