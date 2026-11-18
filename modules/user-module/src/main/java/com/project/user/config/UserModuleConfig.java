package com.project.user.config;

import com.project.common.datetime.config.AppDateTimeProperties;
import com.project.common.datetime.converter.PersianDateConverter;
import com.project.common.datetime.formatter.PersianDateFormatter;
import com.project.common.datetime.parser.PersianDateParser;
import com.project.common.logging.audit.AuditRecorder;
import com.project.common.persistence.sql.SqlDialectSupport;
import com.project.common.search.jdbc.JdbcSearchExecutor;
import com.project.common.search.limits.SearchLimits;
import com.project.common.search.metadata.SearchMetadataRegistry;
import com.project.common.security.authentication.AuthenticationUserPort;
import com.project.user.dao.PermissionQueryDao;
import com.project.user.dao.RoleRepository;
import com.project.user.dao.UserRepository;
import com.project.user.dao.UserRoleRepository;
import com.project.user.dao.impl.PermissionQueryJdbcDao;
import com.project.user.dao.impl.UserJpaDao;
import com.project.user.dao.impl.UserReportJdbcDao;
import com.project.user.interface_.dao.UserDao;
import com.project.user.interface_.dao.UserReportDao;
import com.project.user.interface_.service.UserService;
import com.project.user.mapper.UserMapper;
import com.project.user.security.UserAuthenticationAdapter;
import com.project.user.service.impl.UserServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Wiring of the user module.
 *
 * <p>DAO interfaces are bound to their implementations here (and only here), which is what
 * makes swapping a persistence strategy a configuration change instead of a refactor.</p>
 */
@Configuration(proxyBeanMethods = false)
public class UserModuleConfig {

    @Bean
    public UserDao userDao(UserRepository userRepository,
                              SearchMetadataRegistry metadataRegistry,
                              SearchLimits searchLimits,
                              AppDateTimeProperties dateTimeProperties) {
        return new UserJpaDao(userRepository, metadataRegistry, searchLimits, dateTimeProperties);
    }

    @Bean
    public UserReportDao userReportJdbcDao(NamedParameterJdbcTemplate jdbcTemplate,
                                               JdbcClient jdbcClient,
                                               SqlDialectSupport dialect,
                                               JdbcSearchExecutor searchExecutor,
                                               SearchMetadataRegistry metadataRegistry) {
        return new UserReportJdbcDao(jdbcTemplate, jdbcClient, dialect, searchExecutor, metadataRegistry);
    }

    @Bean
    public PermissionQueryDao permissionQueryDao(NamedParameterJdbcTemplate jdbcTemplate,
                                                 JdbcClient jdbcClient,
                                                 SqlDialectSupport dialect) {
        return new PermissionQueryJdbcDao(jdbcTemplate, jdbcClient, dialect);
    }

    @Bean
    public UserService userService(UserDao userDao,
                                   UserReportDao userReportJdbcDao,
                                   UserRoleRepository userRoleRepository,
                                   RoleRepository roleRepository,
                                   PasswordEncoder passwordEncoder,
                                   UserMapper userMapper,
                                   PersianDateParser persianDateParser,
                                   PersianDateConverter persianDateConverter,
                                   PersianDateFormatter persianDateFormatter,
                                   AuditRecorder auditRecorder) {
        return new UserServiceImpl(userDao, userReportJdbcDao, userRoleRepository, roleRepository,
                passwordEncoder, userMapper, persianDateParser, persianDateConverter,
                persianDateFormatter, auditRecorder);
    }

    @Bean
    public AuthenticationUserPort authenticationUserPort(UserRepository userRepository,
                                                         PermissionQueryDao permissionQueryDao) {
        return new UserAuthenticationAdapter(userRepository, permissionQueryDao);
    }
}
