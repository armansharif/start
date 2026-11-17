package com.project.common.search.jdbc;

import com.project.common.core.paging.PageResult;
import com.project.common.core.search.BaseSearchModel;
import com.project.common.persistence.sql.SqlDialectSupport;
import com.project.common.search.limits.SearchLimits;
import com.project.common.search.metadata.SearchMetadata;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Objects;

/**
 * Executes a built search fragment against the database.
 *
 * <p>The DAO owns the SELECT and the {@link RowMapper}; this class only appends the
 * generated WHERE/ORDER BY/pagination. That split keeps SQL readability with developers
 * while making the dynamic part generated and safe.</p>
 */
public class JdbcSearchExecutor {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SqlSearchBuilder builder;
    private final SqlDialectSupport dialect;
    private final SearchLimits limits;

    public JdbcSearchExecutor(NamedParameterJdbcTemplate jdbcTemplate,
                              SqlSearchBuilder builder,
                              SqlDialectSupport dialect,
                              SearchLimits limits) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
        this.builder = Objects.requireNonNull(builder, "builder must not be null");
        this.dialect = Objects.requireNonNull(dialect, "dialect must not be null");
        this.limits = Objects.requireNonNull(limits, "limits must not be null");
    }

    public <T> PageResult<T> search(String selectSql,
                                    String countSql,
                                    SearchMetadata metadata,
                                    BaseSearchModel model,
                                    RowMapper<T> rowMapper) {
        SqlSearchFragment fragment = builder.build(metadata, model);

        String where = fragment.hasWhereClause() ? "\nWHERE " + fragment.whereClause() : "";
        String orderBy = fragment.hasOrderBy() ? "\nORDER BY " + fragment.orderByClause() : "";

        int page = model.getPage();
        int size = Math.min(model.getSize(), limits.getMaxPageSize());
        MapSqlParameterSource params = fragment.params();

        if (model.isIgnorePagination() && limits.isAllowIgnorePagination()) {
            List<T> content = jdbcTemplate.query(selectSql + where + orderBy, params, rowMapper);
            return PageResult.of(content, 0, content.size(), content.size());
        }

        long offset = (long) page * size;
        String pagedSql = selectSql + where + orderBy + "\n" + dialect.limitOffset(size, offset);
        List<T> content = jdbcTemplate.query(pagedSql, params, rowMapper);

        long total = countSql == null ? content.size()
                : count(countSql + where, params);
        return PageResult.of(content, page, size, total);
    }

    private long count(String countSql, MapSqlParameterSource params) {
        Long value = jdbcTemplate.queryForObject(countSql, params, Long.class);
        return value == null ? 0L : value;
    }
}
