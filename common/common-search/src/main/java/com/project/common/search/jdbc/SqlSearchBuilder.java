package com.project.common.search.jdbc;

import com.project.common.core.error.BusinessException;
import com.project.common.core.error.CommonErrorCode;
import com.project.common.core.search.BaseSearchModel;
import com.project.common.core.search.MatchOperator;
import com.project.common.core.search.SearchConstraint;
import com.project.common.core.search.SearchFilterModel;
import com.project.common.core.search.SearchSortModel;
import com.project.common.datetime.model.DateRange;
import com.project.common.search.SearchValidation;
import com.project.common.search.annotation.SearchableModel;
import com.project.common.search.limits.SearchLimits;
import com.project.common.search.metadata.SearchFieldMetadata;
import com.project.common.search.metadata.SearchMetadata;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Turns a search model into a SQL where/order-by fragment using only whitelisted columns.
 *
 * <p>Security model:</p>
 * <ul>
 *   <li>field names are resolved against {@link SearchMetadata}; anything unknown is rejected;</li>
 *   <li>columns come from the annotation, never from the request;</li>
 *   <li>values are always bound as named parameters;</li>
 *   <li>{@code %}, {@code _} and the escape character inside LIKE patterns are escaped;</li>
 *   <li>limits on filters, list sizes, sorts and keyword length are enforced first.</li>
 * </ul>
 */
public class SqlSearchBuilder {

    private static final char LIKE_ESCAPE = '\\';

    private final SearchLimits limits;
    private final ZoneId zone;

    public SqlSearchBuilder(SearchLimits limits, ZoneId zone) {
        this.limits = limits;
        this.zone = zone;
    }

    public SqlSearchFragment build(SearchMetadata metadata, BaseSearchModel model) {
        SearchValidation.checkRequest(model, limits);

        MapSqlParameterSource params = new MapSqlParameterSource();
        List<String> predicates = new ArrayList<>();
        int[] parameterIndex = {0};

        for (SearchFilterModel filter : model.getFilter()) {
            SearchFieldMetadata field = requireJdbcField(metadata, filter.getFieldName());
            List<String> constraints = new ArrayList<>();
            for (SearchConstraint constraint : filter.getConstraints()) {
                constraints.add(buildConstraint(field, constraint, params, parameterIndex));
            }
            if (!constraints.isEmpty()) {
                String joiner = filter.getLogicalOperator() == com.project.common.core.search.LogicalOperator.OR
                        ? " OR " : " AND ";
                predicates.add("(" + String.join(joiner, constraints) + ")");
            }
        }

        addKeywordPredicate(metadata, model, params, predicates, parameterIndex);
        addDateRangePredicate(metadata, model, params, predicates, parameterIndex);

        String whereClause = predicates.isEmpty() ? "" : String.join(" AND ", predicates);
        String orderByClause = buildOrderBy(metadata, model);

        return new SqlSearchFragment(whereClause, params, orderByClause);
    }

    private SearchFieldMetadata requireJdbcField(SearchMetadata metadata, String clientName) {
        SearchFieldMetadata field = SearchValidation.requireField(metadata, clientName);
        if (!field.supportsJdbc()) {
            // Fail closed: a field without a mapped column cannot be filtered in SQL.
            throw new BusinessException(CommonErrorCode.SEARCH_INVALID_FIELD, clientName);
        }
        return field;
    }

    private String buildConstraint(SearchFieldMetadata field,
                                   SearchConstraint constraint,
                                   MapSqlParameterSource params,
                                   int[] index) {
        String column = field.column();
        MatchOperator operator = constraint.operator();
        Object value = constraint.fieldValue();

        return switch (operator) {
            case IS_NULL -> column + " IS NULL";
            case IS_NOT_NULL -> column + " IS NOT NULL";
            case IS_EMPTY -> "(" + column + " IS NULL OR " + column + " = '')";
            case IS_NOT_EMPTY -> "(" + column + " IS NOT NULL AND " + column + " <> '')";
            case EQUALS -> column + " = " + bind(params, index, value);
            case NOT_EQUALS -> "(" + column + " IS NULL OR " + column + " <> " + bind(params, index, value) + ")";
            case LIKE -> column + " LIKE " + bind(params, index, likePattern(value, true, true)) + " ESCAPE '\\'";
            case NOT_LIKE -> "(" + column + " IS NULL OR " + column + " NOT LIKE "
                    + bind(params, index, likePattern(value, true, true)) + " ESCAPE '\\')";
            case STARTS_WITH -> column + " LIKE " + bind(params, index, likePattern(value, false, true)) + " ESCAPE '\\'";
            case ENDS_WITH -> column + " LIKE " + bind(params, index, likePattern(value, true, false)) + " ESCAPE '\\'";
            case GREATER_THAN -> column + " > " + bind(params, index, value);
            case GREATER_THAN_OR_EQUAL -> column + " >= " + bind(params, index, value);
            case LESS_THAN -> column + " < " + bind(params, index, value);
            case LESS_THAN_OR_EQUAL -> column + " <= " + bind(params, index, value);
            case BETWEEN -> between(column, constraint, params, index);
            case IN -> inClause(column, false, toCollection(value, operator), params, index);
            case NOT_IN -> inClause(column, true, toCollection(value, operator), params, index);
        };
    }

    private String between(String column, SearchConstraint constraint,
                           MapSqlParameterSource params, int[] index) {
        List<Object> pair = constraint.asPair();
        if (pair == null) {
            throw new BusinessException(CommonErrorCode.SEARCH_INVALID_OPERATOR, MatchOperator.BETWEEN.name());
        }
        return column + " BETWEEN " + bind(params, index, pair.get(0))
                + " AND " + bind(params, index, pair.get(1));
    }

    private String inClause(String column, boolean negated, Collection<?> values,
                            MapSqlParameterSource params, int[] index) {
        if (values.isEmpty()) {
            // An empty IN list must never silently match nothing/everything.
            throw new BusinessException(CommonErrorCode.SEARCH_INVALID_OPERATOR,
                    negated ? MatchOperator.NOT_IN.name() : MatchOperator.IN.name());
        }
        List<String> placeholders = new ArrayList<>(values.size());
        for (Object value : values) {
            placeholders.add(bind(params, index, value));
        }
        String in = column + " IN (" + String.join(", ", placeholders) + ")";
        return negated ? "(" + column + " IS NULL OR " + column + " NOT IN ("
                + String.join(", ", placeholders) + "))" : in;
    }

    private void addKeywordPredicate(SearchMetadata metadata,
                                     BaseSearchModel model,
                                     MapSqlParameterSource params,
                                     List<String> predicates,
                                     int[] index) {
        String keyword = model.getKeyword();
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        List<SearchFieldMetadata> keywordFields = metadata.keywordFields().stream()
                .filter(SearchFieldMetadata::supportsJdbc)
                .toList();
        if (keywordFields.isEmpty()) {
            return;
        }
        List<String> likes = new ArrayList<>();
        Object pattern = likePattern(keyword, true, true);
        for (SearchFieldMetadata field : keywordFields) {
            likes.add(field.column() + " LIKE " + bind(params, index, pattern) + " ESCAPE '\\'");
        }
        predicates.add("(" + String.join(" OR ", likes) + ")");
    }

    private void addDateRangePredicate(SearchMetadata metadata,
                                       BaseSearchModel model,
                                       MapSqlParameterSource params,
                                       List<String> predicates,
                                       int[] index) {
        if (!metadata.hasDateColumn()) {
            return;
        }
        LocalDate from = model.getFromDate();
        LocalDate to = model.getToDate();
        if (from == null && to == null) {
            return;
        }
        if (from != null && to != null && to.isBefore(from)) {
            throw new BusinessException(CommonErrorCode.SEARCH_INVALID_OPERATOR, "dateRange");
        }
        DateRange range = from == null ? DateRange.of(to, to)
                : to == null ? DateRange.of(from, from) : DateRange.of(from, to);

        String column = metadata.dateColumn();
        if (metadata.dateColumnType() == SearchableModel.DateColumnType.DATE) {
            predicates.add(column + " >= " + bind(params, index, range.fromInclusive()));
            predicates.add(column + " <= " + bind(params, index, range.toInclusive()));
        } else {
            Instant start = range.startInstant(zone);
            Instant endExclusive = range.endExclusiveInstant(zone);
            predicates.add(column + " >= " + bind(params, index, start));
            predicates.add(column + " < " + bind(params, index, endExclusive));
        }
    }

    private String buildOrderBy(SearchMetadata metadata, BaseSearchModel model) {
        List<SearchSortModel> sorts = model.getSort();
        if (sorts.isEmpty() && model.getSortBy() != null && !model.getSortBy().isBlank()) {
            sorts = List.of(new SearchSortModel(model.getSortBy(), model.getSortDirection()));
        }
        if (sorts.isEmpty()) {
            return "";
        }
        if (sorts.size() > limits.getMaxSortFields()) {
            throw new BusinessException(CommonErrorCode.SEARCH_LIMIT_EXCEEDED);
        }
        List<String> clauses = new ArrayList<>();
        for (SearchSortModel sort : sorts) {
            SearchFieldMetadata field = requireJdbcField(metadata, sort.getFieldName());
            if (!field.sortable()) {
                throw new BusinessException(CommonErrorCode.SEARCH_INVALID_FIELD, sort.getFieldName());
            }
            clauses.add(field.column() + " " + sort.getDirection().name());
        }
        return String.join(", ", clauses);
    }

    private static String bind(MapSqlParameterSource params, int[] index, Object value) {
        String name = "p" + index[0]++;
        params.addValue(name, value);
        return ":" + name;
    }

    /**
     * Builds a LIKE pattern with escaped wildcards, so a user searching for "50%" does not
     * turn the rest of the query into a wildcard.
     */
    static String likePattern(Object value, boolean leadingWildcard, boolean trailingWildcard) {
        String text = String.valueOf(value);
        StringBuilder escaped = new StringBuilder(text.length() + 8);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == LIKE_ESCAPE || c == '%' || c == '_') {
                escaped.append(LIKE_ESCAPE);
            }
            escaped.append(c);
        }
        return (leadingWildcard ? "%" : "") + escaped + (trailingWildcard ? "%" : "");
    }

    private static Collection<?> toCollection(Object value, MatchOperator operator) {
        if (value instanceof Collection<?> collection) {
            return collection;
        }
        throw new BusinessException(CommonErrorCode.SEARCH_INVALID_OPERATOR, operator.name());
    }
}
