package com.project.common.logging.audit;

import com.project.common.persistence.dao.BaseJpaRepository;

import java.util.List;

/**
 * Repository for audit entries. Query methods stay minimal; reporting is done with SQL.
 */
public interface AuditLogRepository extends BaseJpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityNameAndEntityIdOrderByEventTimestampDesc(String entityName, String entityId);
}
