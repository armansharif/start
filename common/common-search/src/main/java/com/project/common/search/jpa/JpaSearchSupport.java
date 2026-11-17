package com.project.common.search.jpa;

import com.project.common.core.error.BusinessException;
import com.project.common.core.error.CommonErrorCode;
import com.project.common.core.search.BaseSearchModel;
import com.project.common.core.search.SearchSortModel;
import com.project.common.search.SearchValidation;
import com.project.common.search.limits.SearchLimits;
import com.project.common.search.metadata.SearchFieldMetadata;
import com.project.common.search.metadata.SearchMetadata;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps search sorting/paging onto Spring Data, keeping only whitelisted, sortable fields.
 */
public final class JpaSearchSupport {

    private JpaSearchSupport() {
    }

    public static Sort sort(SearchMetadata metadata, BaseSearchModel model) {
        List<SearchSortModel> sorts = model.getSort();
        if (sorts.isEmpty() && model.getSortBy() != null && !model.getSortBy().isBlank()) {
            sorts = List.of(new SearchSortModel(model.getSortBy(), model.getSortDirection()));
        }
        List<Sort.Order> orders = new ArrayList<>();
        for (SearchFieldMetadata field : SearchValidation.sortableFields(metadata, withSorts(model, sorts))) {
            SearchSortModel sort = sorts.get(orders.size());
            orders.add(new Sort.Order(Sort.Direction.valueOf(sort.getDirection().name()), field.fieldName()));
        }
        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }

    public static Pageable pageable(BaseSearchModel model, SearchMetadata metadata, SearchLimits limits) {
        int size = Math.min(model.getSize(), limits.getMaxPageSize());
        return PageRequest.of(model.getPage(), Math.max(size, 1), sort(metadata, model));
    }

    private static BaseSearchModel withSorts(BaseSearchModel model, List<SearchSortModel> sorts) {
        if (!sorts.equals(model.getSort())) {
            sorts.forEach(model::addSort);
        }
        return model;
    }

    public static void validatePageSize(BaseSearchModel model, SearchLimits limits) {
        if (model.getSize() > limits.getMaxPageSize()) {
            throw new BusinessException(CommonErrorCode.PAGINATION_LIMIT_EXCEEDED);
        }
    }
}
