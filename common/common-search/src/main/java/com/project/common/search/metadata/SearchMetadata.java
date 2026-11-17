package com.project.common.search.metadata;

import com.project.common.search.annotation.SearchableModel;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Immutable search metadata of one search model.
 *
 * <p>Built once at startup and cached; requests only do map lookups, never reflection.
 * This is what makes the field whitelist cheap enough to apply on every request.</p>
 */
public final class SearchMetadata {

    private final Class<?> modelType;
    private final Map<String, SearchFieldMetadata> fieldsByName;
    private final List<SearchFieldMetadata> fields;
    private final String dateColumn;
    private final String dateProperty;
    private final SearchableModel.DateColumnType dateColumnType;

    SearchMetadata(Class<?> modelType,
                   Collection<SearchFieldMetadata> fields,
                   String dateColumn,
                   String dateProperty,
                   SearchableModel.DateColumnType dateColumnType) {
        this.modelType = modelType;
        this.fields = List.copyOf(fields);
        this.dateColumn = dateColumn;
        this.dateProperty = dateProperty;
        this.dateColumnType = dateColumnType;

        Map<String, SearchFieldMetadata> index = new LinkedHashMap<>();
        for (SearchFieldMetadata field : fields) {
            index.putIfAbsent(field.publicName().toLowerCase(), field);
            if (field.alias() != null && !field.alias().isBlank()) {
                index.putIfAbsent(field.alias().toLowerCase(), field);
            }
            index.putIfAbsent(field.fieldName().toLowerCase(), field);
        }
        this.fieldsByName = Map.copyOf(index);
    }

    public Class<?> modelType() {
        return modelType;
    }

    public List<SearchFieldMetadata> fields() {
        return fields;
    }

    /**
     * @return metadata of the field the client asked for, or empty when it is not allowed
     */
    public Optional<SearchFieldMetadata> find(String clientName) {
        if (clientName == null || clientName.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(fieldsByName.get(clientName.trim().toLowerCase()));
    }

    public List<SearchFieldMetadata> keywordFields() {
        return fields.stream().filter(SearchFieldMetadata::keywordSearch).toList();
    }

    public boolean hasDateColumn() {
        return dateColumn != null && !dateColumn.isBlank();
    }

    public String dateColumn() {
        return dateColumn;
    }

    public boolean hasDateProperty() {
        return dateProperty != null && !dateProperty.isBlank();
    }

    public String dateProperty() {
        return dateProperty;
    }

    public SearchableModel.DateColumnType dateColumnType() {
        return dateColumnType;
    }
}
