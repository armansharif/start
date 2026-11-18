package com.project.common.search.jdbc;

import com.project.common.core.error.BusinessException;
import com.project.common.core.error.CommonErrorCode;
import com.project.common.core.search.LogicalOperator;
import com.project.common.core.search.MatchOperator;
import com.project.common.core.search.SearchFilterModel;
import com.project.common.core.search.SortDirection;
import com.project.common.search.TestSearchModels;
import com.project.common.search.limits.SearchLimits;
import com.project.common.search.metadata.SearchMetadata;
import com.project.common.search.metadata.SearchMetadataRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SqlSearchBuilderTest {

    private static final ZoneId TEHRAN = ZoneId.of("Asia/Tehran");

    private TestSearchModels.CustomerSearchModel model;
    private SearchMetadata metadata;
    private SearchLimits limits;
    private SqlSearchBuilder builder;

    @BeforeEach
    void setUp() {
        limits = new SearchLimits();
        builder = new SqlSearchBuilder(limits, TEHRAN);
        metadata = new SearchMetadataRegistry().metadataOf(TestSearchModels.CustomerSearchModel.class);
        model = new TestSearchModels.CustomerSearchModel();
    }

    @Test
    void buildsEqualityPredicateWithBoundParameter() {
        model.addFilter("name", LogicalOperator.AND, "ali", MatchOperator.EQUALS);

        SqlSearchFragment fragment = builder.build(metadata, model);

        assertThat(fragment.whereClause()).isEqualTo("(NAME = :p0)");
        assertThat(fragment.params().getValue("p0")).isEqualTo("ali");
    }

    @Test
    void escapesLikeWildcards() {
        model.addFilter("name", LogicalOperator.AND, "50%_x", MatchOperator.LIKE);

        SqlSearchFragment fragment = builder.build(metadata, model);

        assertThat(fragment.whereClause()).contains("LIKE :p0 ESCAPE '\\'");
        assertThat(fragment.params().getValue("p0")).isEqualTo("%50\\%\\_x%");
    }

    @Test
    void combinesConstraintsWithOrInsideAFilter() {
        model.addFilter(SearchFilterModel.of("name", LogicalOperator.OR, "ali", MatchOperator.LIKE)
                .addConstraint("reza", MatchOperator.LIKE));

        SqlSearchFragment fragment = builder.build(metadata, model);

        assertThat(fragment.whereClause()).isEqualTo("(NAME LIKE :p0 ESCAPE '\\' OR NAME LIKE :p1 ESCAPE '\\')");
        assertThat(fragment.params().getValue("p1")).isEqualTo("%reza%");
    }

    @Test
    void combinesFiltersWithAnd() {
        model.addFilter("name", LogicalOperator.AND, "ali", MatchOperator.LIKE);
        model.addFilter("status", LogicalOperator.AND, "ACTIVE", MatchOperator.EQUALS);

        SqlSearchFragment fragment = builder.build(metadata, model);

        assertThat(fragment.whereClause()).isEqualTo("(NAME LIKE :p0 ESCAPE '\\') AND (STATUS = :p1)");
    }

    @Test
    void buildsNullAndEmptyPredicates() {
        model.addFilter("status", LogicalOperator.AND, null, MatchOperator.IS_EMPTY);

        SqlSearchFragment fragment = builder.build(metadata, model);

        assertThat(fragment.whereClause()).isEqualTo("((STATUS IS NULL OR STATUS = ''))");
        assertThat(fragment.params().hasValue("p0")).isFalse();
    }

    @Test
    void buildsInList() {
        model.addFilter("status", LogicalOperator.AND, List.of("A", "B"), MatchOperator.IN);

        SqlSearchFragment fragment = builder.build(metadata, model);

        assertThat(fragment.whereClause()).isEqualTo("(STATUS IN (:p0, :p1))");
    }

    @Test
    void rejectsUnknownField() {
        model.addFilter("nationalCode", LogicalOperator.AND, "123", MatchOperator.EQUALS);

        assertThatThrownBy(() -> builder.build(metadata, model))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(CommonErrorCode.SEARCH_INVALID_FIELD);
    }

    @Test
    void rejectsFieldWithoutColumn() {
        model.addFilter("jpaOnlyField", LogicalOperator.AND, "x", MatchOperator.EQUALS);

        assertThatThrownBy(() -> builder.build(metadata, model))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(CommonErrorCode.SEARCH_INVALID_FIELD);
    }

    @Test
    void rejectsEmptyInList() {
        model.addFilter("status", LogicalOperator.AND, List.of(), MatchOperator.IN);

        assertThatThrownBy(() -> builder.build(metadata, model)).isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectsTooManyFilters() {
        limits.setMaxFilters(2);
        for (int i = 0; i < 3; i++) {
            model.addFilter("name", LogicalOperator.AND, "x" + i, MatchOperator.EQUALS);
        }

        assertThatThrownBy(() -> builder.build(metadata, model))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(CommonErrorCode.SEARCH_LIMIT_EXCEEDED);
    }

    @Test
    void rejectsTooLargeInList() {
        limits.setMaxInListSize(2);
        model.addFilter("status", LogicalOperator.AND, List.of("A", "B", "C"), MatchOperator.IN);

        assertThatThrownBy(() -> builder.build(metadata, model))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(CommonErrorCode.SEARCH_LIMIT_EXCEEDED);
    }

    @Test
    void neverPlacesRawValuesIntoTheClause() {
        String attack = "x'; DROP TABLE CUSTOMER; --";
        model.addFilter("name", LogicalOperator.AND, attack, MatchOperator.EQUALS);

        SqlSearchFragment fragment = builder.build(metadata, model);

        assertThat(fragment.whereClause()).doesNotContain(attack).doesNotContain("DROP");
        assertThat(fragment.params().getValue("p0")).isEqualTo(attack);
    }

    @Test
    void buildsSortAndRejectsNonSortableFields() {
        model.addSort("name", SortDirection.DESC);

        assertThat(builder.build(metadata, model).orderByClause()).isEqualTo("NAME DESC");

        TestSearchModels.CustomerSearchModel other = new TestSearchModels.CustomerSearchModel();
        other.addSort("age", SortDirection.ASC);

        assertThatThrownBy(() -> builder.build(metadata, other))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(CommonErrorCode.SEARCH_INVALID_FIELD);
    }

    @Test
    void buildsKeywordOverKeywordFieldsOnly() {
        model.setKeyword("ali");

        SqlSearchFragment fragment = builder.build(metadata, model);

        assertThat(fragment.whereClause()).isEqualTo("(NAME LIKE :p0 ESCAPE '\\')");
        assertThat(fragment.params().getValue("p0")).isEqualTo("%ali%");
    }

    @Test
    void convertsDateRangeToHalfOpenInstantsInConfiguredZone() {
        model.setFromDate(LocalDate.of(2026, 11, 15));
        model.setToDate(LocalDate.of(2026, 11, 15));

        SqlSearchFragment fragment = builder.build(metadata, model);

        assertThat(fragment.whereClause()).contains("CREATED_DATE >= :p0").contains("CREATED_DATE < :p1");
        assertThat(fragment.params().getValue("p0"))
                .isEqualTo(Instant.parse("2026-11-14T20:30:00Z"));
        assertThat(fragment.params().getValue("p1"))
                .isEqualTo(Instant.parse("2026-11-15T20:30:00Z"));
    }

    @Test
    void keepsDateTypeColumnsAsCalendarDates() {
        SearchMetadata dateMetadata = new SearchMetadataRegistry()
                .metadataOf(TestSearchModels.PersonSearchModel.class);
        TestSearchModels.PersonSearchModel person = new TestSearchModels.PersonSearchModel();
        person.setFromDate(LocalDate.of(1990, 1, 1));

        SqlSearchFragment fragment = builder.build(dateMetadata, person);

        assertThat(fragment.whereClause()).contains("BIRTH_DATE >= :p0").contains("BIRTH_DATE <= :p1");
        assertThat(fragment.params().getValue("p0")).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    void rejectsInvertedDateRange() {
        model.setFromDate(LocalDate.of(2026, 11, 16));
        model.setToDate(LocalDate.of(2026, 11, 15));

        assertThatThrownBy(() -> builder.build(metadata, model)).isInstanceOf(BusinessException.class);
    }
}
