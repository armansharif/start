package com.project.common.search.limits;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Hard limits of the dynamic search engine.
 *
 * <p>Every limit exists because the alternative is an unbounded query built from client
 * input. They are configuration, not constants, so an installation can tighten them
 * without a code change.</p>
 */
@ConfigurationProperties(prefix = "app.search")
public class SearchLimits {

    private int maxFilters = 20;
    private int maxConstraintsPerFilter = 10;
    private int maxInListSize = 500;
    private int maxSortFields = 3;
    private int maxKeywordLength = 100;
    private int maxPageSize = 100;
    private boolean allowIgnorePagination = false;

    public int getMaxFilters() {
        return maxFilters;
    }

    public void setMaxFilters(int maxFilters) {
        this.maxFilters = maxFilters;
    }

    public int getMaxConstraintsPerFilter() {
        return maxConstraintsPerFilter;
    }

    public void setMaxConstraintsPerFilter(int maxConstraintsPerFilter) {
        this.maxConstraintsPerFilter = maxConstraintsPerFilter;
    }

    public int getMaxInListSize() {
        return maxInListSize;
    }

    public void setMaxInListSize(int maxInListSize) {
        this.maxInListSize = maxInListSize;
    }

    public int getMaxSortFields() {
        return maxSortFields;
    }

    public void setMaxSortFields(int maxSortFields) {
        this.maxSortFields = maxSortFields;
    }

    public int getMaxKeywordLength() {
        return maxKeywordLength;
    }

    public void setMaxKeywordLength(int maxKeywordLength) {
        this.maxKeywordLength = maxKeywordLength;
    }

    public int getMaxPageSize() {
        return maxPageSize;
    }

    public void setMaxPageSize(int maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    public boolean isAllowIgnorePagination() {
        return allowIgnorePagination;
    }

    public void setAllowIgnorePagination(boolean allowIgnorePagination) {
        this.allowIgnorePagination = allowIgnorePagination;
    }
}
