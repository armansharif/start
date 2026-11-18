package com.project.common.search;

import com.project.common.core.search.BaseSearchModel;
import com.project.common.core.search.MatchOperator;
import com.project.common.search.annotation.Searchable;
import com.project.common.search.annotation.SearchableModel;

/**
 * Search models used by the unit tests. Kept next to the tests so production code is not
 * polluted with fixtures.
 */
public final class TestSearchModels {

    private TestSearchModels() {
    }

    @SearchableModel(dateColumn = "CREATED_DATE", dateProperty = "createdDate",
            dateColumnType = SearchableModel.DateColumnType.TIMESTAMP)
    public static class CustomerSearchModel extends BaseSearchModel {

        @Searchable(column = "NAME", sortable = true, keywordSearch = true)
        private String name;

        @Searchable(column = "STATUS", sortable = true)
        private String status;

        @Searchable(column = "AGE", operator = MatchOperator.GREATER_THAN_OR_EQUAL)
        private Integer age;

        @Searchable(name = "group", alias = "customerGroup", column = "GROUP_ID")
        private Long groupId;

        @Searchable
        private String jpaOnlyField;

        /** Not annotated on purpose: must never be searchable. */
        private String nationalCode;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSecret() {
            return nationalCode;
        }
    }

    @SearchableModel(dateColumn = "BIRTH_DATE", dateProperty = "birthDate",
            dateColumnType = SearchableModel.DateColumnType.DATE)
    public static class PersonSearchModel extends BaseSearchModel {

        @Searchable(column = "FULL_NAME")
        private String fullName;
    }

    public static class ChildSearchModel extends CustomerSearchModel {
    }
}
