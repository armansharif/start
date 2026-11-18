package com.project.common.search.config;

import com.project.common.datetime.config.AppDateTimeProperties;
import com.project.common.persistence.sql.SqlDialectSupport;
import com.project.common.search.jdbc.JdbcSearchExecutor;
import com.project.common.search.jdbc.SqlSearchBuilder;
import com.project.common.search.limits.SearchLimits;
import com.project.common.search.metadata.SearchMetadataRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Search engine wiring.
 *
 * <p>The registry is a singleton with an internal cache; builders are stateless and get
 * the configured zone so that calendar day ranges are resolved consistently (Persian
 * dates enter the system here already converted).</p>
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SearchLimits.class)
public class SearchConfig {

    @Bean
    public SearchMetadataRegistry searchMetadataRegistry() {
        return new SearchMetadataRegistry();
    }

    @Bean
    public SqlSearchBuilder sqlSearchBuilder(SearchLimits limits, AppDateTimeProperties dateTimeProperties) {
        return new SqlSearchBuilder(limits, dateTimeProperties.getZone());
    }

    @Bean
    public JdbcSearchExecutor jdbcSearchExecutor(NamedParameterJdbcTemplate jdbcTemplate,
                                                 SqlSearchBuilder sqlSearchBuilder,
                                                 SqlDialectSupport dialect,
                                                 SearchLimits limits) {
        return new JdbcSearchExecutor(jdbcTemplate, sqlSearchBuilder, dialect, limits);
    }
}
