package com.project.common.persistence.sql;

import java.util.Objects;

/**
 * Produces the small set of SQL fragments that differ between supported databases.
 *
 * <p>Only pagination is implemented today, because it is the one construct every
 * report needs. Anything else database specific stays inside a DAO implementation and
 * is documented there.</p>
 */
public class SqlDialectSupport {

    private final SqlDialect dialect;

    public SqlDialectSupport(SqlDialect dialect) {
        this.dialect = Objects.requireNonNull(dialect, "dialect must not be null");
    }

    public SqlDialect dialect() {
        return dialect;
    }

    /**
     * @param limit  maximum number of rows
     * @param offset number of rows to skip
     * @return the pagination clause to append to a SELECT statement
     */
    public String limitOffset(long limit, long offset) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive: " + limit);
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset must not be negative: " + offset);
        }
        return switch (dialect) {
            case MYSQL -> "LIMIT " + limit + " OFFSET " + offset;
            case ORACLE -> "OFFSET " + offset + " ROWS FETCH NEXT " + limit + " ROWS ONLY";
        };
    }
}
