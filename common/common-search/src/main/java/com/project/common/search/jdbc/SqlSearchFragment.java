package com.project.common.search.jdbc;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * Result of building a dynamic search as SQL text.
 *
 * <p>The where clause only ever contains whitelisted column names and named parameter
 * placeholders; every client value travels in {@code params}. Concatenating values into
 * the clause is never allowed.</p>
 *
 * @param whereClause     clause without the {@code WHERE} keyword, or empty
 * @param params          named parameters referenced by the clause
 * @param orderByClause   order by without the {@code ORDER BY} keyword, or empty
 */
public record SqlSearchFragment(String whereClause, MapSqlParameterSource params, String orderByClause) {

    public boolean hasWhereClause() {
        return whereClause != null && !whereClause.isBlank();
    }

    public boolean hasOrderBy() {
        return orderByClause != null && !orderByClause.isBlank();
    }
}
