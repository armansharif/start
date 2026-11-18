package com.project.common.search.metadata;

import com.project.common.search.TestSearchModels;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearchMetadataRegistryTest {

    private final SearchMetadataRegistry registry = new SearchMetadataRegistry();

    @Test
    void indexesAnnotatedFieldsOnly() {
        SearchMetadata metadata = registry.metadataOf(TestSearchModels.CustomerSearchModel.class);

        assertThat(metadata.fields()).extracting(SearchFieldMetadata::fieldName)
                .contains("name", "status", "age", "groupId", "jpaOnlyField")
                .doesNotContain("nationalCode");
    }

    @Test
    void resolvesPublicNameAliasAndFieldName() {
        SearchMetadata metadata = registry.metadataOf(TestSearchModels.CustomerSearchModel.class);

        assertThat(metadata.find("group")).get().extracting(SearchFieldMetadata::fieldName).isEqualTo("groupId");
        assertThat(metadata.find("customerGroup")).isPresent();
        assertThat(metadata.find("groupId")).isPresent();
        assertThat(metadata.find("GROUP")).isPresent();
        assertThat(metadata.find("nationalCode")).isEmpty();
        assertThat(metadata.find(null)).isEmpty();
    }

    @Test
    void indexesInheritedFields() {
        SearchMetadata metadata = registry.metadataOf(TestSearchModels.ChildSearchModel.class);

        assertThat(metadata.find("name")).isPresent();
        assertThat(metadata.find("status")).isPresent();
    }

    @Test
    void cachesPerModelAndKeepsDateOptions() {
        SearchMetadata first = registry.metadataOf(TestSearchModels.CustomerSearchModel.class);
        SearchMetadata second = registry.metadataOf(TestSearchModels.CustomerSearchModel.class);

        assertThat(first).isSameAs(second);
        assertThat(first.hasDateColumn()).isTrue();
        assertThat(first.dateColumn()).isEqualTo("CREATED_DATE");
        assertThat(first.hasDateProperty()).isTrue();
        assertThat(first.dateColumnType()).isEqualTo(com.project.common.search.annotation.SearchableModel.DateColumnType.TIMESTAMP);

        SearchMetadata person = registry.metadataOf(TestSearchModels.PersonSearchModel.class);
        assertThat(person.dateColumnType())
                .isEqualTo(com.project.common.search.annotation.SearchableModel.DateColumnType.DATE);
    }

    @Test
    void flagsFieldsWithoutColumn() {
        SearchMetadata metadata = registry.metadataOf(TestSearchModels.CustomerSearchModel.class);

        assertThat(metadata.find("jpaOnlyField")).get()
                .extracting(SearchFieldMetadata::supportsJdbc).isEqualTo(false);
        assertThat(metadata.find("name")).get()
                .extracting(SearchFieldMetadata::supportsJdbc).isEqualTo(true);
    }
}
