package com.project.common.persistence.sql;

/**
 * The databases this platform knows how to talk to.
 *
 * <p>MySQL is the current and fully supported target. Oracle is a documented future
 * option, not a concurrent target: only the few constructs that cannot be expressed
 * portably (pagination, identity, some functions) are isolated behind this enum.</p>
 */
public enum SqlDialect {

    MYSQL,
    ORACLE
}
