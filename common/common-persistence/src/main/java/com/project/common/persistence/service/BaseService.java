package com.project.common.persistence.service;

import com.project.common.core.paging.PageResult;
import com.project.common.persistence.entity.BaseEntity;

/**
 * Minimal read/delete contract for plain CRUD services.
 *
 * <p>It deliberately contains no create/update methods: those always require DTO to
 * entity mapping and business rules, so they stay in the module service. Services that
 * are not plain CRUD should not implement this interface at all.</p>
 *
 * @param <T>  entity type
 * @param <ID> identifier type
 */
public interface BaseService<T extends BaseEntity, ID> {

    T getById(ID id);

    PageResult<T> getAll(int page, int size);

    void delete(ID id);

    long count();
}
