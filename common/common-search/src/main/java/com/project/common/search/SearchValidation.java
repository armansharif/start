package com.project.common.search;

import com.project.common.core.error.BusinessException;
import com.project.common.core.error.CommonErrorCode;
import com.project.common.core.search.BaseSearchModel;
import com.project.common.core.search.SearchConstraint;
import com.project.common.core.search.SearchFilterModel;
import com.project.common.search.limits.SearchLimits;
import com.project.common.search.metadata.SearchFieldMetadata;
import com.project.common.search.metadata.SearchMetadata;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Applies the active limits and resolves fields against the whitelist.
 *
 * <p>Both strategies (JPA and JDBC) call this before building anything, so a request is
 * either fully valid or rejected — never partially applied.</p>
 */
public final class SearchValidation {

    private SearchValidation() {
    }

    public static void checkRequest(BaseSearchModel model, SearchLimits limits) {
        List<SearchFilterModel> filters = model.getFilter();
        if (filters.size() > limits.getMaxFilters()) {
            throw limitExceeded();
        }
        long constraints = filters.stream().mapToInt(filter -> filter.getConstraints().size()).sum();
        if (constraints > (long) limits.getMaxFilters() * limits.getMaxConstraintsPerFilter()) {
            throw limitExceeded();
        }
        for (SearchFilterModel filter : filters) {
            if (filter.getConstraints().size() > limits.getMaxConstraintsPerFilter()) {
                throw limitExceeded();
            }
            for (SearchConstraint constraint : filter.getConstraints()) {
                checkConstraintValues(constraint, limits);
            }
        }
        if (model.getSort().size() > limits.getMaxSortFields()) {
            throw limitExceeded();
        }
        String keyword = model.getKeyword();
        if (keyword != null && keyword.length() > limits.getMaxKeywordLength()) {
            throw limitExceeded();
        }
        if (model.isIgnorePagination() && !limits.isAllowIgnorePagination()) {
            throw limitExceeded();
        }
    }

    public static SearchFieldMetadata requireField(SearchMetadata metadata, String clientName) {
        return metadata.find(clientName)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.SEARCH_INVALID_FIELD, clientName));
    }

    public static List<SearchFieldMetadata> sortableFields(SearchMetadata metadata, BaseSearchModel model) {
        return model.getSort().stream()
                .map(sort -> {
                    SearchFieldMetadata field = requireField(metadata, sort.getFieldName());
                    if (!field.sortable()) {
                        throw new BusinessException(CommonErrorCode.SEARCH_INVALID_FIELD, sort.getFieldName());
                    }
                    return field;
                })
                .toList();
    }

    private static void checkConstraintValues(SearchConstraint constraint, SearchLimits limits) {
        if (constraint.operator() == null) {
            throw new BusinessException(CommonErrorCode.SEARCH_INVALID_OPERATOR, "null");
        }
        Object value = constraint.fieldValue();
        switch (constraint.operator().arity()) {
            case NONE -> {
                // no value expected
            }
            case SINGLE -> {
                if (value == null) {
                    throw new BusinessException(CommonErrorCode.SEARCH_INVALID_OPERATOR,
                            constraint.operator().name());
                }
            }
            case PAIR -> {
                if (!(value instanceof Collection<?> collection) || collection.size() != 2) {
                    throw new BusinessException(CommonErrorCode.SEARCH_INVALID_OPERATOR,
                            constraint.operator().name());
                }
            }
            case COLLECTION -> {
                if (!(value instanceof Collection<?> collection)) {
                    throw new BusinessException(CommonErrorCode.SEARCH_INVALID_OPERATOR,
                            constraint.operator().name());
                }
                if (collection.size() > limits.getMaxInListSize()) {
                    throw limitExceeded();
                }
                if (collection.isEmpty()) {
                    throw new BusinessException(CommonErrorCode.SEARCH_INVALID_OPERATOR,
                            constraint.operator().name());
                }
            }
        }
    }

    public static Map<String, Object> emptyParams() {
        return Map.of();
    }

    private static BusinessException limitExceeded() {
        return new BusinessException(CommonErrorCode.SEARCH_LIMIT_EXCEEDED);
    }
}
