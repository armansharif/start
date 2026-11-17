package com.project.common.web.api;

import com.project.common.core.paging.PageResult;

import java.util.List;
import java.util.function.Function;

/**
 * Transport representation of a page. Kept separate from {@link PageResult} so the core
 * module stays free of web concerns and the API shape can evolve independently.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext) {

    public static <T> PageResponse<T> from(PageResult<T> result) {
        return new PageResponse<>(result.getContent(), result.getPage(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(), result.isHasNext());
    }

    public static <S, T> PageResponse<T> from(PageResult<S> result, Function<? super S, ? extends T> mapper) {
        PageResult<T> mapped = result.map(mapper);
        return from(mapped);
    }
}
