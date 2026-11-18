package com.project.user.dao.impl;

import com.project.common.persistence.dao.AbstractJdbcDao;
import com.project.common.persistence.sql.SqlDialectSupport;
import com.project.user.dao.PermissionQueryDao;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * JDBC implementation of the authority lookups.
 */
public class PermissionQueryJdbcDao extends AbstractJdbcDao implements PermissionQueryDao {

    private static final String ROLE_CODES_SQL = """
            SELECT r.CODE
              FROM ROLES r
              JOIN USER_ROLE ur ON ur.ROLE_ID = r.ID
             WHERE ur.USER_ID = :userId
            """;

    private static final String PERMISSION_CODES_SQL = """
            SELECT DISTINCT p.CODE
              FROM PERMISSIONS p
              JOIN ROLE_PERMISSION rp ON rp.PERMISSION_ID = p.ID
              JOIN USER_ROLE ur ON ur.ROLE_ID = rp.ROLE_ID
             WHERE ur.USER_ID = :userId
            """;

    public PermissionQueryJdbcDao(NamedParameterJdbcTemplate jdbcTemplate,
                                  JdbcClient jdbcClient,
                                  SqlDialectSupport dialect) {
        super(jdbcTemplate, jdbcClient, dialect);
    }

    @Override
    public Set<String> roleCodesOf(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        MapSqlParameterSource params = new MapSqlParameterSource("userId", userId);
        List<String> codes = queryList(ROLE_CODES_SQL, params, (rs, rowNum) -> rs.getString("CODE"));
        return new LinkedHashSet<>(codes);
    }

    @Override
    public Set<String> permissionCodesOf(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        MapSqlParameterSource params = new MapSqlParameterSource("userId", userId);
        List<String> codes = queryList(PERMISSION_CODES_SQL, params, (rs, rowNum) -> rs.getString("CODE"));
        return new LinkedHashSet<>(codes);
    }
}
