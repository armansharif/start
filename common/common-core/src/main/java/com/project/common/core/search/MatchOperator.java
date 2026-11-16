package com.project.common.core.search;

/**
 * Compares a single field with a value.
 *
 * <p>Each operator declares whether it needs a value, how many values it needs and
 * whether it is a range/collection operator. Query builders use this metadata
 * instead of hard-coding per-operator branches in several places.</p>
 */
public enum MatchOperator {

    EQUALS(ValueArity.SINGLE),
    NOT_EQUALS(ValueArity.SINGLE),
    LIKE(ValueArity.SINGLE),
    NOT_LIKE(ValueArity.SINGLE),
    STARTS_WITH(ValueArity.SINGLE),
    ENDS_WITH(ValueArity.SINGLE),
    IN(ValueArity.COLLECTION),
    NOT_IN(ValueArity.COLLECTION),
    GREATER_THAN(ValueArity.SINGLE),
    GREATER_THAN_OR_EQUAL(ValueArity.SINGLE),
    LESS_THAN(ValueArity.SINGLE),
    LESS_THAN_OR_EQUAL(ValueArity.SINGLE),
    BETWEEN(ValueArity.PAIR),
    IS_NULL(ValueArity.NONE),
    IS_NOT_NULL(ValueArity.NONE),
    IS_EMPTY(ValueArity.NONE),
    IS_NOT_EMPTY(ValueArity.NONE);

    /** How many values the operator consumes. */
    public enum ValueArity {
        NONE, SINGLE, PAIR, COLLECTION
    }

    private final ValueArity arity;

    MatchOperator(ValueArity arity) {
        this.arity = arity;
    }

    public ValueArity arity() {
        return arity;
    }

    public boolean requiresValue() {
        return arity != ValueArity.NONE;
    }
}
