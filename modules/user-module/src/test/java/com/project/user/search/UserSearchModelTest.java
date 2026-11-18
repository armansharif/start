package com.project.user.search;

import com.project.common.search.annotation.Searchable;
import com.project.common.search.annotation.SearchableModel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserSearchModelTest {

    @Test
    void exposesOnlyAnnotatedFields() throws NoSuchFieldException {
        assertThat(UserSearchModel.class.getDeclaredField("username").getAnnotation(Searchable.class)).isNotNull();
        assertThat(UserSearchModel.class.getDeclaredField("fullName").getAnnotation(Searchable.class)).isNotNull();

        assertThatThrownBy(() -> UserSearchModel.class.getDeclaredField("passwordHash"))
                .isInstanceOf(NoSuchFieldException.class);
    }

    @Test
    void declaresTheDateColumnUsedByRangeFilters() {
        SearchableModel model = UserSearchModel.class.getAnnotation(SearchableModel.class);

        assertThat(model).isNotNull();
        assertThat(model.dateColumn()).isEqualTo("CREATED_DATE");
        assertThat(model.dateProperty()).isEqualTo("createdDate");
    }
}
