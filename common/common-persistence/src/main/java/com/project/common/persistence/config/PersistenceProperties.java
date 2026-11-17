package com.project.common.persistence.config;

import com.project.common.persistence.paging.PaginationSupport;
import com.project.common.persistence.sql.SqlDialect;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Persistence level configuration shared by every module.
 */
@ConfigurationProperties(prefix = "app.persistence")
public class PersistenceProperties {

    /** Target database. MySQL is the supported one; Oracle only changes SQL fragments. */
    private SqlDialect dialect = SqlDialect.MYSQL;

    /** Page size used when a request does not specify one. */
    private int defaultPageSize = PaginationSupport.DEFAULT_PAGE_SIZE;

    /** Hard upper bound for page size; larger requests are rejected or clamped. */
    private int maxPageSize = PaginationSupport.DEFAULT_MAX_PAGE_SIZE;

    /** JDBC fetch size for streaming/read-only queries. */
    private int fetchSize = 100;

    public SqlDialect getDialect() {
        return dialect;
    }

    public void setDialect(SqlDialect dialect) {
        this.dialect = dialect;
    }

    public int getDefaultPageSize() {
        return defaultPageSize;
    }

    public void setDefaultPageSize(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }

    public int getMaxPageSize() {
        return maxPageSize;
    }

    public void setMaxPageSize(int maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    public int getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }
}
