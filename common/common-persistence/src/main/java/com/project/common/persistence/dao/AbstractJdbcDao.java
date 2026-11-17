package com.project.common.persistence.dao;

import com.project.common.core.paging.PageResult;
import com.project.common.persistence.sql.SqlDialectSupport;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Base class for read-oriented DAO implementations backed by plain SQL.
 *
 * <p>Used for reports, multi-table queries and bulk operations where JPA would be
 * slower or less transparent. All parameters are bound by name; no SQL text is ever
 * built from user input (see the dynamic search engine for the whitelisted variant).</p>
 *
 * <p>Because the platform uses a single {@code JpaTransactionManager}, JDBC calls made
 * inside a service transaction participate in that same transaction automatically.</p>
 */
public abstract class AbstractJdbcDao {

    protected final NamedParameterJdbcTemplate jdbcTemplate;
    protected final JdbcClient jdbcClient;
    protected final SqlDialectSupport dialect;

    protected AbstractJdbcDao(NamedParameterJdbcTemplate jdbcTemplate,
                              JdbcClient jdbcClient,
                              SqlDialectSupport dialect) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
        this.jdbcClient = Objects.requireNonNull(jdbcClient, "jdbcClient must not be null");
        this.dialect = Objects.requireNonNull(dialect, "dialect must not be null");
    }

    protected <T> List<T> queryList(String sql, MapSqlParameterSource params, RowMapper<T> rowMapper) {
        return jdbcTemplate.query(sql, params, rowMapper);
    }

    protected <T> Optional<T> queryOne(String sql, MapSqlParameterSource params, RowMapper<T> rowMapper) {
        List<T> rows = jdbcTemplate.query(sql, params, rowMapper);
        return rows.stream().findFirst();
    }

    protected long queryCount(String countSql, MapSqlParameterSource params) {
        Long count = jdbcTemplate.queryForObject(countSql, params, Long.class);
        return count == null ? 0L : count;
    }

    protected int update(String sql, MapSqlParameterSource params) {
        return jdbcTemplate.update(sql, params);
    }

    protected int[] batchUpdate(String sql, SqlParameterSource[] batch) {
        return jdbcTemplate.batchUpdate(sql, batch);
    }

    /**
     * Runs a paginated query.
     *
     * @param selectSql  SELECT with placeholders but without pagination
     * @param countSql   matching COUNT query, or {@code null} to skip counting
     * @param params     named parameters
     * @param rowMapper  explicit row mapping; no reflection, no Map based mapping
     */
    protected <T> PageResult<T> queryPage(String selectSql,
                                          String countSql,
                                          MapSqlParameterSource params,
                                          RowMapper<T> rowMapper,
                                          int page,
                                          int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        long offset = (long) safePage * safeSize;

        String pagedSql = selectSql + "\n" + dialect.limitOffset(safeSize, offset);
        List<T> content = jdbcTemplate.query(pagedSql, params, rowMapper);

        long total = countSql == null ? content.size() : queryCount(countSql, params);
        return PageResult.of(content, safePage, safeSize, total);
    }
}
