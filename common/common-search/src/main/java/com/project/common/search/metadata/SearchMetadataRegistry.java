package com.project.common.search.metadata;

import com.project.common.core.search.BaseSearchModel;
import com.project.common.search.annotation.Searchable;
import com.project.common.search.annotation.SearchableModel;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builds and caches the search metadata of every model.
 *
 * <p>Discovery is annotation driven and includes inherited fields, so a module only
 * declares what is specific to it. The cache is bounded by the number of search models
 * in the application, which is a compile-time constant.</p>
 */
public class SearchMetadataRegistry {

    private final Map<Class<?>, SearchMetadata> cache = new ConcurrentHashMap<>();

    public SearchMetadata metadataOf(Class<? extends BaseSearchModel> modelType) {
        Objects.requireNonNull(modelType, "modelType must not be null");
        return cache.computeIfAbsent(modelType, this::build);
    }

    public SearchMetadata metadataOf(BaseSearchModel model) {
        Objects.requireNonNull(model, "model must not be null");
        return metadataOf(model.getClass());
    }

    private SearchMetadata build(Class<?> modelType) {
        List<SearchFieldMetadata> fields = new ArrayList<>();
        for (Class<?> type = modelType; type != null && type != Object.class; type = type.getSuperclass()) {
            if (type == BaseSearchModel.class) {
                continue;
            }
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }
                Searchable searchable = field.getAnnotation(Searchable.class);
                if (searchable == null) {
                    continue;
                }
                fields.add(toMetadata(field, searchable));
            }
        }

        SearchableModel modelAnnotation = modelType.getAnnotation(SearchableModel.class);
        String dateColumn = modelAnnotation == null ? "" : modelAnnotation.dateColumn();
        String dateProperty = modelAnnotation == null ? "" : modelAnnotation.dateProperty();
        SearchableModel.DateColumnType dateColumnType = modelAnnotation == null
                ? SearchableModel.DateColumnType.TIMESTAMP
                : modelAnnotation.dateColumnType();

        return new SearchMetadata(modelType, fields, dateColumn, dateProperty, dateColumnType);
    }

    private static SearchFieldMetadata toMetadata(Field field, Searchable searchable) {
        String publicName = searchable.name().isBlank() ? field.getName() : searchable.name();
        String alias = searchable.alias().isBlank() ? null : searchable.alias();
        String column = searchable.column().isBlank() ? null : searchable.column();
        return new SearchFieldMetadata(field.getName(), publicName, alias, column,
                searchable.operator(), searchable.sortable(), searchable.keywordSearch(),
                field.getType());
    }
}
