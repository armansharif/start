package com.project.user.dao.impl;

import com.project.common.core.paging.PageResult;
import com.project.common.datetime.config.AppDateTimeProperties;
import com.project.common.persistence.dao.AbstractJpaDao;
import com.project.common.search.jpa.JpaSearchSpecification;
import com.project.common.search.jpa.JpaSearchSupport;
import com.project.common.search.limits.SearchLimits;
import com.project.common.search.metadata.SearchMetadataRegistry;
import com.project.user.dao.UserRepository;
import com.project.user.entity.User;
import com.project.user.interface_.dao.UserDao;
import com.project.user.search.UserSearchModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * JPA implementation of {@link UserDao}.
 *
 * <p>Plain CRUD comes from the shared base class; the dynamic search is expressed as a
 * specification built from the search metadata, so no field reaches the query unless it is
 * annotated as searchable.</p>
 */
public class UserJpaDao extends AbstractJpaDao<User, Long> implements UserDao {

    private final UserRepository userRepository;
    private final SearchMetadataRegistry metadataRegistry;
    private final SearchLimits searchLimits;
    private final AppDateTimeProperties dateTimeProperties;

    public UserJpaDao(UserRepository userRepository,
                      SearchMetadataRegistry metadataRegistry,
                      SearchLimits searchLimits,
                      AppDateTimeProperties dateTimeProperties) {
        super(userRepository, User.class);
        this.userRepository = userRepository;
        this.metadataRegistry = metadataRegistry;
        this.searchLimits = searchLimits;
        this.dateTimeProperties = dateTimeProperties;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByUsernameIgnoreCase(username.trim().toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean existsByUsername(String username) {
        return username != null && userRepository.existsByUsernameIgnoreCase(username.trim());
    }

    @Override
    public boolean existsByMobile(String mobile) {
        return mobile != null && !mobile.isBlank() && userRepository.existsByMobile(mobile.trim());
    }

    @Override
    public PageResult<User> search(UserSearchModel searchModel) {
        JpaSearchSupport.validatePageSize(searchModel, searchLimits);

        var metadata = metadataRegistry.metadataOf(searchModel);
        var specification = JpaSearchSpecification.<User>of(metadata, searchModel, searchLimits,
                dateTimeProperties.getZone());
        Pageable pageable = JpaSearchSupport.pageable(searchModel, metadata, searchLimits);

        Page<User> page = userRepository.findAll(specification, pageable);
        return PageResult.of(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Override
    public List<User> findAllByIdIn(Collection<Long> ids) {
        return ids == null || ids.isEmpty() ? List.of() : userRepository.findAllById(ids);
    }
}
