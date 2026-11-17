package com.project.common.search.metadata;

import com.project.common.core.search.MatchOperator;

/**
 * Resolved metadata of one searchable field.
 *
 * @param fieldName     Java field name; the JPA property path
 * @param publicName    name accepted from clients
 * @param alias         alternative accepted name, or {@code null}
 * @param column        physical column for the SQL strategy, or {@code null} when JPA only
 * @param defaultOperator comparison used for bare values
 * @param sortable      whether sorting by this field is allowed
 * @param keywordSearch whether the free-text keyword covers this field
 * @param javaType      declared field type, used to convert incoming values
 */
public record SearchFieldMetadata(
        String fieldName,
        String publicName,
        String alias,
        String column,
        MatchOperator defaultOperator,
        boolean sortable,
        boolean keywordSearch,
        Class<?> javaType) {

    public boolean supportsJdbc() {
        return column != null && !column.isBlank();
    }

    public boolean matches(String candidate) {
        return publicName.equalsIgnoreCase(candidate)
                || (alias != null && alias.equalsIgnoreCase(candidate))
                || fieldName.equalsIgnoreCase(candidate);
    }
}
