package com.project.common.core.search;

import java.util.Objects;

/**
 * One sort instruction. The field name is resolved against search metadata before
 * it reaches any query; unknown or non sortable fields are rejected.
 */
public class SearchSortModel {

    private final String fieldName;
    private SortDirection direction;

    public SearchSortModel(String fieldName, SortDirection direction) {
        this.fieldName = Objects.requireNonNull(fieldName, "fieldName must not be null");
        this.direction = direction == null ? SortDirection.ASC : direction;
    }

    public static SearchSortModel asc(String fieldName) {
        return new SearchSortModel(fieldName, SortDirection.ASC);
    }

    public static SearchSortModel desc(String fieldName) {
        return new SearchSortModel(fieldName, SortDirection.DESC);
    }

    public String getFieldName() {
        return fieldName;
    }

    public SortDirection getDirection() {
        return direction;
    }

    public void setDirection(SortDirection direction) {
        this.direction = direction == null ? SortDirection.ASC : direction;
    }
}
