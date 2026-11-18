package com.project.common.logging.errorlog;

import com.project.common.persistence.dao.BaseJpaRepository;

/**
 * Spring Data repository for system error entries.
 */
public interface SystemErrorLogRepository extends BaseJpaRepository<SystemErrorLog, Long> {
}
