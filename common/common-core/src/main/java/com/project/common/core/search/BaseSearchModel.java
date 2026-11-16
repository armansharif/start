package com.project.common.core.search;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Base class for every module's search request.
 *
 * <p>Modules extend it with their own criteria and mark the ones that may be used
 * by clients with {@code @Searchable}. The properties declared here are the common
 * transport-level criteria; anything business specific belongs to the subclass.</p>
 *
 * <p>The model is executor agnostic: it holds no SQL and no JPA types, so the same
 * request can be served by the JPA strategy or the JDBC strategy.</p>
 */
public abstract class BaseSearchModel {

    public static final int DEFAULT_PAGE_SIZE = 20;

    private Integer page = 0;
    private Integer size = DEFAULT_PAGE_SIZE;
    private String sortBy;
    private SortDirection sortDirection = SortDirection.ASC;
    private String keyword;
    private LocalDate fromDate;
    private LocalDate toDate;
    private boolean ignorePagination;

    private final List<SearchSortModel> sort = new ArrayList<>();
    private final List<SearchFilterModel> filter = new ArrayList<>();

    // ------------------------------------------------------------------ filters

    public BaseSearchModel addFilter(String fieldName, LogicalOperator logicalOperator,
                                     Object value, MatchOperator matchOperator) {
        filter.add(SearchFilterModel.of(fieldName, logicalOperator, value, matchOperator));
        return this;
    }

    public BaseSearchModel addFilter(SearchFilterModel searchFilter) {
        if (searchFilter != null) {
            filter.add(searchFilter);
        }
        return this;
    }

    public BaseSearchModel addFilter(List<SearchFilterModel> searchFilters) {
        if (searchFilters != null) {
            searchFilters.stream().filter(Objects::nonNull).forEach(filter::add);
        }
        return this;
    }

    /**
     * Copies every non-null property of {@code source} into a filter.
     *
     * <p>Intended for the common "search by this DTO" case. Fields declared by
     * {@link BaseSearchModel}, static/transient fields, Java runtime fields and the
     * names listed in {@code ignoreFields} are skipped. Values become
     * {@code EQUALS} filters unless the search metadata declares another default.</p>
     */
    public BaseSearchModel addFilterByObject(Object source, String... ignoreFields) {
        if (source == null) {
            return this;
        }
        List<String> ignored = new ArrayList<>(Arrays.asList(ignoreFields));
        for (Class<?> type = source.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            if (type == BaseSearchModel.class) {
                continue;
            }
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())
                        || Modifier.isTransient(field.getModifiers())
                        || field.isSynthetic()
                        || field.getName().startsWith("$")
                        || containsIgnoreCase(ignored, field.getName())) {
                    continue;
                }
                Object value = readField(field, source);
                if (value == null) {
                    continue;
                }
                if (value instanceof CharSequence text && text.toString().isBlank()) {
                    continue;
                }
                filter.add(SearchFilterModel.of(field.getName(), LogicalOperator.AND, value, MatchOperator.EQUALS));
            }
        }
        return this;
    }

    public Object getFilterFirstValue(String fieldName) {
        if (fieldName == null) {
            return null;
        }
        return filter.stream()
                .filter(f -> fieldName.equalsIgnoreCase(f.getFieldName()))
                .flatMap(f -> f.getConstraints().stream())
                .map(SearchConstraint::fieldValue)
                .findFirst()
                .orElse(null);
    }

    public boolean removeFilter(String fieldName) {
        if (fieldName == null) {
            return false;
        }
        return filter.removeIf(f -> fieldName.equalsIgnoreCase(f.getFieldName()));
    }

    public List<SearchFilterModel> getFilter() {
        return List.copyOf(filter);
    }

    // -------------------------------------------------------------------- sorts

    public BaseSearchModel addSort(String fieldName, SortDirection direction) {
        sort.add(new SearchSortModel(fieldName, direction));
        return this;
    }

    public BaseSearchModel addSort(SearchSortModel searchSort) {
        if (searchSort != null) {
            sort.add(searchSort);
        }
        return this;
    }

    public List<SearchSortModel> getSort() {
        return List.copyOf(sort);
    }

    // --------------------------------------------------------------- properties

    public int getPage() {
        return page == null ? 0 : Math.max(page, 0);
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public int getSize() {
        return size == null || size <= 0 ? DEFAULT_PAGE_SIZE : size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public SortDirection getSortDirection() {
        return sortDirection == null ? SortDirection.ASC : sortDirection;
    }

    public void setSortDirection(SortDirection sortDirection) {
        this.sortDirection = sortDirection;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public boolean isIgnorePagination() {
        return ignorePagination;
    }

    public BaseSearchModel setIgnorePagination(boolean ignorePagination) {
        this.ignorePagination = ignorePagination;
        return this;
    }

    // ------------------------------------------------------------------ helpers

    private static Object readField(Field field, Object source) {
        try {
            field.setAccessible(true);
            return field.get(source);
        } catch (IllegalAccessException | RuntimeException ex) {
            return null;
        } finally {
            // Nothing to restore: 'setAccessible' is only used for local DTO instances.
        }
    }

    private static boolean containsIgnoreCase(List<String> candidates, String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        return candidates.stream()
                .filter(Objects::nonNull)
                .anyMatch(candidate -> candidate.toLowerCase(Locale.ROOT).equals(normalized));
    }
}
