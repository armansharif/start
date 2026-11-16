package com.project.common.core.search;

/**
 * Combines conditions. Deliberately separate from {@link MatchOperator}, which
 * compares a field with a value: mixing the two concepts in one enum makes the
 * query builders ambiguous and error prone.
 */
public enum LogicalOperator {

    /** All conditions of the filter must match. */
    AND,

    /** At least one condition of the filter must match. */
    OR
}
