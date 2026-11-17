package com.project.common.persistence.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BaseEntityTest {

    private static final class SampleEntity extends BaseEntity {
    }

    @Test
    void newEntityHasNoIdAndIsTransient() {
        SampleEntity entity = new SampleEntity();

        assertThat(entity.getId()).isNull();
        assertThat(entity.isNew()).isTrue();
        assertThat(entity.getCreatedDate()).isNull();
        assertThat(entity.getVersion()).isNull();
    }

    @Test
    void transientEntitiesAreOnlyEqualToThemselves() {
        SampleEntity first = new SampleEntity();
        SampleEntity second = new SampleEntity();

        assertThat(first).isEqualTo(first);
        assertThat(first).isNotEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(System.identityHashCode(first));
    }

    @Test
    void differentEntityTypesAreNeverEqual() {
        SampleEntity first = new SampleEntity();
        BaseEntity second = new BaseEntity() {
        };

        assertThat(first).isNotEqualTo(second);
    }
}
