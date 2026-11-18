package com.project.common.persistence.config;

import com.project.common.core.security.CurrentUserProvider;
import com.project.common.persistence.audit.CurrentUserAuditorAware;
import com.project.common.persistence.sql.SqlDialectSupport;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Persistence wiring.
 *
 * <p>Transaction management is intentionally left to Spring Boot's auto-configuration:
 * with a single {@code DataSource} and a single {@code EntityManagerFactory} it registers
 * one {@link org.springframework.orm.jpa.JpaTransactionManager}, which also publishes the
 * JDBC connection as the transaction bound resource. Adding a second
 * {@code DataSourceTransactionManager} would let JPA and JDBC run in separate
 * transactions, which is exactly what this platform must avoid.</p>
 */
@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing(auditorAwareRef = "currentUserAuditorAware")
// Entities and repositories live in the modules, not under the application's package, so
// both scans are widened to the project root. Each module only contributes its own types.
@EnableJpaRepositories(basePackages = "com.project")
@EntityScan(basePackages = "com.project")
@EnableConfigurationProperties(PersistenceProperties.class)
public class PersistenceConfig {

    @Bean
    public AuditorAware<Long> currentUserAuditorAware(
            ObjectProvider<CurrentUserProvider> currentUserProvider) {
        return new CurrentUserAuditorAware(currentUserProvider);
    }

    @Bean
    public SqlDialectSupport sqlDialectSupport(PersistenceProperties properties) {
        return new SqlDialectSupport(properties.getDialect());
    }
}
