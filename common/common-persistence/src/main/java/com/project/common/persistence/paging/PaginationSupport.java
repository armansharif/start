package com.project.common.persistence.paging;

/**
 * Guards paging input so a client cannot ask for an unbounded result set.
 *
 * <p>The limit is a configuration value ({@code app.persistence.max-page-size}); the
 * defaults here are only fallbacks for callers that have no configuration at hand.</p>
 */
public final class PaginationSupport {

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int DEFAULT_MAX_PAGE_SIZE = 100;

    private PaginationSupport() {
    }

    public static int safePage(int page) {
        return Math.max(page, 0);
    }

    public static int safeSize(int size, int maxPageSize) {
        int effectiveMax = maxPageSize > 0 ? maxPageSize : DEFAULT_MAX_PAGE_SIZE;
        if (size <= 0) {
            return Math.min(DEFAULT_PAGE_SIZE, effectiveMax);
        }
        return Math.min(size, effectiveMax);
    }

    public static long offset(int page, int size) {
        return (long) safePage(page) * safeSize(size, DEFAULT_MAX_PAGE_SIZE);
    }
}
