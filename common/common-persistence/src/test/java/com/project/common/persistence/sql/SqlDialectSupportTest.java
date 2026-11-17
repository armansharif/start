package com.project.common.persistence.sql;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SqlDialectSupportTest {

    @Test
    void buildsMysqlPaginationClause() {
        SqlDialectSupport support = new SqlDialectSupport(SqlDialect.MYSQL);
        assertThat(support.limitOffset(20, 40)).isEqualTo("LIMIT 20 OFFSET 40");
    }

    @Test
    void buildsOraclePaginationClause() {
        SqlDialectSupport support = new SqlDialectSupport(SqlDialect.ORACLE);
        assertThat(support.limitOffset(20, 40)).isEqualTo("OFFSET 40 ROWS FETCH NEXT 20 ROWS ONLY");
    }

    @Test
    void rejectsInvalidBounds() {
        SqlDialectSupport support = new SqlDialectSupport(SqlDialect.MYSQL);
        assertThatThrownBy(() -> support.limitOffset(0, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> support.limitOffset(10, -1)).isInstanceOf(IllegalArgumentException.class);
    }
}
