package com.project.common.persistence.dao;

import com.project.common.core.paging.PageResult;
import com.project.common.persistence.entity.BaseEntity;

import java.util.List;
import java.util.Optional;

/**
 * Contract for the plain repositories of the platform.
 *
 * <p>Services depend on this abstraction (or on a module specific interface extending
 * it), never on JPA, JDBC or SQL details. Search is intentionally absent: dynamic search
 * lives in {@code common-search} so that {@code common-persistence} does not depend on it.</p>
 *
 * @param <T>  entity type
 * @param <ID> identifier type
 */
public interface BaseDao<T extends BaseEntity, ID> {

    Class<T> getEntityType();

    Optional<T> findById(ID id);

    /**
     * @throws com.project.common.core.error.ResourceNotFoundException when no row exists
     */
    T requireById(ID id);

    List<T> findAll();

    PageResult<T> findAll(int page, int size);

    boolean existsById(ID id);

    long count();

    T save(T entity);

    /**
     * Updates an existing entity.
     *
     * @throws com.project.common.core.error.ResourceNotFoundException when the row does not exist
     */
    T update(T entity);

    boolean deleteById(ID id);

    void delete(T entity);

    /**
     * Forces pending JPA changes to the database.
     *
     * <p>Required when a JPA write is followed by a JDBC read in the same transaction:
     * both share one connection, but Hibernate may not have flushed yet.</p>
     */
    void flush();
}
