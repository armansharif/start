package com.project.common.logging.config;

import com.project.common.core.security.CurrentUserProvider;
import com.project.common.logging.audit.AuditLogRepository;
import com.project.common.logging.audit.AuditRecorder;
import com.project.common.logging.correlation.CorrelationIdFilter;
import com.project.common.logging.correlation.MdcTaskDecorator;
import com.project.common.logging.errorlog.JpaSystemErrorRecorder;
import com.project.common.logging.errorlog.SystemErrorLogRepository;
import com.project.common.web.error.SystemErrorRecorder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;

import java.time.Clock;

/**
 * Logging, error recording and audit wiring.
 *
 * <p>The correlation filter is declared as a bean, not registered with an order: Spring
 * Boot places it before the security filter chain, which is what the error handling
 * relies on (a 401 must already carry a correlation id).</p>
 */
@Configuration(proxyBeanMethods = false)
public class LoggingConfig {

    @Bean
    public CorrelationIdFilter correlationIdFilter() {
        return new CorrelationIdFilter();
    }

    @Bean
    public TaskDecorator mdcTaskDecorator() {
        return new MdcTaskDecorator();
    }

    @Bean
    @ConditionalOnMissingBean(SystemErrorRecorder.class)
    public SystemErrorRecorder systemErrorRecorder(SystemErrorLogRepository repository) {
        return new JpaSystemErrorRecorder(repository);
    }

    @Bean
    public AuditRecorder auditRecorder(AuditLogRepository repository,
                                       CurrentUserProvider currentUserProvider,
                                       Clock clock) {
        return new AuditRecorder(repository, currentUserProvider, clock);
    }
}
