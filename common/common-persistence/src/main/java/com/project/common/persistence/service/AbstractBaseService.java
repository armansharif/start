package com.project.common.persistence.service;

import com.project.common.core.paging.PageResult;
import com.project.common.persistence.dao.BaseDao;
import com.project.common.persistence.entity.BaseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Objects;

/**
 * Delegating implementation of {@link BaseService}.
 *
 * <p>One level of inheritance only. Business logic, validation and mapping stay in the
 * module's own service implementation, which may extend this class and add methods.</p>
 */
public abstract class AbstractBaseService<T extends BaseEntity, ID extends Serializable>
        implements BaseService<T, ID> {

    protected final BaseDao<T, ID> dao;

    protected AbstractBaseService(BaseDao<T, ID> dao) {
        this.dao = Objects.requireNonNull(dao, "dao must not be null");
    }

    @Override
    @Transactional(readOnly = true)
    public T getById(ID id) {
        return dao.requireById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<T> getAll(int page, int size) {
        return dao.findAll(page, size);
    }

    @Override
    @Transactional
    public void delete(ID id) {
        if (!dao.deleteById(id)) {
            throw new com.project.common.core.error.ResourceNotFoundException(
                    dao.getEntityType().getSimpleName(), id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return dao.count();
    }
}
