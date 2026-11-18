package com.project.user.dao.impl;

import com.project.common.core.paging.PageResult;
import com.project.common.persistence.dao.AbstractJdbcDao;
import com.project.common.persistence.sql.SqlDialectSupport;
import com.project.common.search.jdbc.JdbcSearchExecutor;
import com.project.common.search.metadata.SearchMetadata;
import com.project.common.search.metadata.SearchMetadataRegistry;
import com.project.user.interface_.dao.UserReportDao;
import com.project.user.search.UserSearchModel;
import com.project.user.view.UserListItemView;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Read model of the user list, served by plain SQL.
 *
 * <p>This is the reference example of the JDBC side of the persistence architecture: a
 * hand written SELECT with an explicit column list and an explicit {@link RowMapper},
 * combined with the shared dynamic search builder.</p>
 */
public class UserReportJdbcDao extends AbstractJdbcDao implements UserReportDao {

    private static final String SELECT_SQL = """
            SELECT u.ID, u.USERNAME, u.FULL_NAME, u.MOBILE, u.STATUS, u.CREATED_DATE
              FROM USERS u
            """;

    private static final String COUNT_SQL = "SELECT COUNT(*) FROM USERS u";

    private static final RowMapper<UserListItemView> ROW_MAPPER = (rs, rowNum) -> new UserListItemView(
            rs.getLong("ID"),
            rs.getString("USERNAME"),
            rs.getString("FULL_NAME"),
            rs.getString("MOBILE"),
            rs.getString("STATUS"),
            rs.getTimestamp("CREATED_DATE") == null ? null : rs.getTimestamp("CREATED_DATE").toInstant());

    private final JdbcSearchExecutor searchExecutor;
    private final SearchMetadata metadata;

    public UserReportJdbcDao(NamedParameterJdbcTemplate jdbcTemplate,
                             JdbcClient jdbcClient,
                             SqlDialectSupport dialect,
                             JdbcSearchExecutor searchExecutor,
                             SearchMetadataRegistry metadataRegistry) {
        super(jdbcTemplate, jdbcClient, dialect);
        this.searchExecutor = searchExecutor;
        this.metadata = metadataRegistry.metadataOf(UserSearchModel.class);
    }

    @Override
    public PageResult<UserListItemView> search(UserSearchModel searchModel) {
        return searchExecutor.search(SELECT_SQL, COUNT_SQL, metadata, searchModel, ROW_MAPPER);
    }
}
