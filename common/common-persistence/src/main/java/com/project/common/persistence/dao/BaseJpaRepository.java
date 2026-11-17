package com.project.common.persistence.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * Common Spring Data repository contract.
 *
 * <p>Module repositories extend this interface, which gives CRUD plus the criteria
 * support the dynamic search engine uses, without defining query methods they do not
 * need. Business queries stay in the module DAO, expressed as SQL or specifications.</p>
 *
 * @param <T>  entity type
 * @param <ID> identifier type
 */
@NoRepositoryBean
public interface BaseJpaRepository<T, ID extends Serializable>
        extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
}
