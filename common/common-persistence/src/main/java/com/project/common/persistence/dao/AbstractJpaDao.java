package com.project.common.persistence.dao;

import com.project.common.core.error.ResourceNotFoundException;
import com.project.common.core.paging.PageResult;
import com.project.common.persistence.entity.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * {@link BaseDao} implementation on top of Spring Data.
 *
 * <p>Only plain CRUD is implemented here. Anything with a business meaning belongs to
 * the module DAO; keeping this class small is a design goal, not an accident.</p>
 */
public abstract class AbstractJpaDao<T extends BaseEntity, ID extends Serializable>
        implements BaseDao<T, ID> {

    protected final BaseJpaRepository<T, ID> repository;
    private final Class<T> entityType;

    protected AbstractJpaDao(BaseJpaRepository<T, ID> repository, Class<T> entityType) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.entityType = Objects.requireNonNull(entityType, "entityType must not be null");
    }

    @Override
    public Class<T> getEntityType() {
        return entityType;
    }

    @Override
    public Optional<T> findById(ID id) {
        return id == null ? Optional.empty() : repository.findById(id);
    }

    @Override
    public T requireById(ID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(entityType.getSimpleName(), id));
    }

    @Override
    public List<T> findAll() {
        return repository.findAll();
    }

    @Override
    public PageResult<T> findAll(int page, int size) {
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        Page<T> result = repository.findAll(pageRequest);
        return PageResult.of(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements());
    }

    @Override
    public boolean existsById(ID id) {
        return id != null && repository.existsById(id);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public T save(T entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        return repository.save(entity);
    }

    @Override
    public T update(T entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        if (entity.getId() == null || !repository.existsById((ID) entity.getId())) {
            throw new ResourceNotFoundException(entityType.getSimpleName(), entity.getId());
        }
        return repository.save(entity);
    }

    @Override
    public boolean deleteById(ID id) {
        if (!existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    @Override
    public void delete(T entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        repository.delete(entity);
    }

    @Override
    public void flush() {
        repository.flush();
    }
}
