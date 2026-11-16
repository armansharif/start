package com.project.common.core.paging;

import java.util.List;
import java.util.Objects;

/**
 * Framework-independent page of results.
 *
 * <p>DAOs and services return this type; the web layer maps it to
 * {@code PageResponse} and adds hypermedia-free pagination metadata.</p>
 *
 * @param <T> element type
 */
public final class PageResult<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;

    private PageResult(List<T> content, int page, int size, long totalElements) {
        this.content = List.copyOf(content);
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
    }

    public static <T> PageResult<T> of(List<T> content, int page, int size, long totalElements) {
        Objects.requireNonNull(content, "content");
        return new PageResult<>(content, Math.max(page, 0), Math.max(size, 1), Math.max(totalElements, 0));
    }

    public static <T> PageResult<T> empty(int page, int size) {
        return new PageResult<>(List.of(), Math.max(page, 0), Math.max(size, 1), 0);
    }

    public static <T> PageResult<T> single(List<T> content) {
        return new PageResult<>(content, 0, Math.max(content.size(), 1), content.size());
    }

    public List<T> getContent() {
        return content;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }

    public boolean isHasNext() {
        return page + 1 < getTotalPages();
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

    public <R> PageResult<R> map(java.util.function.Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        List<R> mapped = content.stream().<R>map(mapper).toList();
        return new PageResult<>(mapped, page, size, totalElements);
    }
}
