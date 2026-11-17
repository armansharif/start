package com.project.common.search.jpa;

import com.project.common.core.error.BusinessException;
import com.project.common.core.error.CommonErrorCode;
import com.project.common.core.search.BaseSearchModel;
import com.project.common.core.search.LogicalOperator;
import com.project.common.core.search.MatchOperator;
import com.project.common.core.search.SearchConstraint;
import com.project.common.core.search.SearchFilterModel;
import com.project.common.datetime.model.DateRange;
import com.project.common.search.SearchValidation;
import com.project.common.search.annotation.SearchableModel;
import com.project.common.search.jdbc.SqlSearchBuilder;
import com.project.common.search.limits.SearchLimits;
import com.project.common.search.metadata.SearchFieldMetadata;
import com.project.common.search.metadata.SearchMetadata;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Builds a JPA {@link Specification} from a search model.
 *
 * <p>Only property paths that exist in {@link SearchMetadata} are used, so a client cannot
 * reach arbitrary attributes. Values are always bound through the criteria API.</p>
 *
 * <p>Intended for entity oriented searches (one root, no joins). Reports and multi table
 * queries belong to the JDBC strategy; attempting joins here would reintroduce the
 * complexity the architecture avoids.</p>
 */
public class JpaSearchSpecification<T> implements Specification<T> {

    private final SearchMetadata metadata;
    private final BaseSearchModel model;
    private final SearchLimits limits;
    private final ZoneId zone;

    public JpaSearchSpecification(SearchMetadata metadata,
                                  BaseSearchModel model,
                                  SearchLimits limits,
                                  ZoneId zone) {
        this.metadata = metadata;
        this.model = model;
        this.limits = limits;
        this.zone = zone;
    }

    public static <T> Specification<T> of(SearchMetadata metadata,
                                          BaseSearchModel model,
                                          SearchLimits limits,
                                          ZoneId zone) {
        return new JpaSearchSpecification<>(metadata, model, limits, zone);
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        SearchValidation.checkRequest(model, limits);

        List<Predicate> predicates = new ArrayList<>();

        for (SearchFilterModel filter : model.getFilter()) {
            SearchFieldMetadata field = SearchValidation.requireField(metadata, filter.getFieldName());
            Path<?> path = root.get(field.fieldName());

            List<Predicate> constraintPredicates = new ArrayList<>();
            for (SearchConstraint constraint : filter.getConstraints()) {
                constraintPredicates.add(toPredicate(cb, path, constraint));
            }
            if (constraintPredicates.isEmpty()) {
                continue;
            }
            predicates.add(filter.getLogicalOperator() == LogicalOperator.OR
                    ? cb.or(constraintPredicates.toArray(Predicate[]::new))
                    : cb.and(constraintPredicates.toArray(Predicate[]::new)));
        }

        addKeywordPredicate(root, cb, predicates);
        addDateRangePredicate(root, cb, predicates);

        return cb.and(predicates.toArray(Predicate[]::new));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate toPredicate(CriteriaBuilder cb, Path<?> path, SearchConstraint constraint) {
        MatchOperator operator = constraint.operator();
        Object value = constraint.fieldValue();

        switch (operator) {
            case IS_NULL:
                return cb.isNull(path);
            case IS_NOT_NULL:
                return cb.isNotNull(path);
            case IS_EMPTY:
                return cb.or(cb.isNull(path), cb.equal(path, ""));
            case IS_NOT_EMPTY:
                return cb.and(cb.isNotNull(path), cb.notEqual(path, ""));
            case EQUALS:
                return cb.equal(path, value);
            case NOT_EQUALS:
                return cb.or(cb.isNull(path), cb.notEqual(path, value));
            case LIKE:
                return cb.like(path.as(String.class), SqlSearchBuilder.likePattern(value, true, true), '\\');
            case NOT_LIKE:
                return cb.or(cb.isNull(path),
                        cb.notLike(path.as(String.class), SqlSearchBuilder.likePattern(value, true, true), '\\'));
            case STARTS_WITH:
                return cb.like(path.as(String.class), SqlSearchBuilder.likePattern(value, false, true), '\\');
            case ENDS_WITH:
                return cb.like(path.as(String.class), SqlSearchBuilder.likePattern(value, true, false), '\\');
            case GREATER_THAN:
                return cb.greaterThan((Expression<? extends Comparable>) path.as(Comparable.class), (Comparable) value);
            case GREATER_THAN_OR_EQUAL:
                return cb.greaterThanOrEqualTo((Expression<? extends Comparable>) path.as(Comparable.class),
                        (Comparable) value);
            case LESS_THAN:
                return cb.lessThan((Expression<? extends Comparable>) path.as(Comparable.class), (Comparable) value);
            case LESS_THAN_OR_EQUAL:
                return cb.lessThanOrEqualTo((Expression<? extends Comparable>) path.as(Comparable.class),
                        (Comparable) value);
            case BETWEEN:
                return between(cb, path, constraint);
            case IN:
                return path.in(requireCollection(value, operator));
            case NOT_IN:
                return cb.or(cb.isNull(path), cb.not(path.in(requireCollection(value, operator))));
            default:
                throw new BusinessException(CommonErrorCode.SEARCH_INVALID_OPERATOR, operator.name());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate between(CriteriaBuilder cb, Path<?> path, SearchConstraint constraint) {
        List<Object> pair = constraint.asPair();
        if (pair == null) {
            throw new BusinessException(CommonErrorCode.SEARCH_INVALID_OPERATOR, MatchOperator.BETWEEN.name());
        }
        Expression<? extends Comparable> expression = (Expression<? extends Comparable>) path.as(Comparable.class);
        return cb.and(cb.greaterThanOrEqualTo(expression, (Comparable) pair.get(0)),
                cb.lessThanOrEqualTo(expression, (Comparable) pair.get(1)));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void addKeywordPredicate(Root<T> root, CriteriaBuilder cb, List<Predicate> predicates) {
        String keyword = model.getKeyword();
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        List<Predicate> likes = new ArrayList<>();
        String pattern = SqlSearchBuilder.likePattern(keyword, true, true);
        for (SearchFieldMetadata field : metadata.keywordFields()) {
            likes.add(cb.like(root.get(field.fieldName()).as(String.class), pattern, '\\'));
        }
        if (!likes.isEmpty()) {
            predicates.add(cb.or(likes.toArray(Predicate[]::new)));
        }
    }

    private void addDateRangePredicate(Root<T> root, CriteriaBuilder cb, List<Predicate> predicates) {
        if (!metadata.hasDateProperty()) {
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

        Path<?> path = root.get(metadata.dateProperty());
        if (metadata.dateColumnType() == SearchableModel.DateColumnType.DATE) {
            Expression<LocalDate> expression = path.as(LocalDate.class);
            predicates.add(cb.greaterThanOrEqualTo(expression, range.fromInclusive()));
            predicates.add(cb.lessThanOrEqualTo(expression, range.toInclusive()));
        } else {
            Expression<Instant> expression = path.as(Instant.class);
            predicates.add(cb.greaterThanOrEqualTo(expression, range.startInstant(zone)));
            predicates.add(cb.lessThan(expression, range.endExclusiveInstant(zone)));
        }
    }

    private static Collection<?> requireCollection(Object value, MatchOperator operator) {
        if (value instanceof Collection<?> collection && !collection.isEmpty()) {
            return collection;
        }
        throw new BusinessException(CommonErrorCode.SEARCH_INVALID_OPERATOR, operator.name());
    }
}
